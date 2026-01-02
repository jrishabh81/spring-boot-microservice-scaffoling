// index.js - renders a user-friendly health landing page using /actuator/health
//
// Responsibilities:
// - Fetch health JSON (/actuator/health) and render an accessible overview
// - Provide clickable component rows that expand to show details
// - Render a canvas-based donut chart for diskSpace (used vs free)
//
// Each top-level function below is documented with a short description of its behaviour.
(function(){
  'use strict';

  /**
   * Root container where the health UI is rendered.
   * @type {HTMLElement}
   */
  const root = document.getElementById('healthRoot');
  /**
   * API path to fetch health JSON from. Uses the /actuator/health endpoint.
   * @type {string}
   */
  const API = '/actuator/health'; // using provided file

  /**
   * el
   * Helper to create an element, apply attributes and append children.
   * Keeps template code compact and reduces repetitive DOM operations.
   *
   * @param {string} tag - Element tag name (e.g., 'div', 'span')
   * @param {Object} [attrs] - Attributes to set. Use key 'class' to set className, 'text' for textContent.
   * @param {...(string|Node)} children - Child nodes or strings appended to the element
   * @returns {HTMLElement} Newly created element with children attached
   */
  function el(tag, attrs, ...children){
    const node = document.createElement(tag);
    if(attrs){
      Object.keys(attrs).forEach(k => {
        if(k === 'class') node.className = attrs[k];
        else if(k === 'text') node.textContent = attrs[k];
        else node.setAttribute(k, attrs[k]);
      });
    }
    children.forEach(c => { if(typeof c === 'string') node.appendChild(document.createTextNode(c)); else if(c) node.appendChild(c); });
    return node;
  }

  /**
   * renderStatusBadge
   * Render a compact status badge showing a textual health status (e.g. UP/DOWN/UNKNOWN).
   * This returns a span element styled by CSS classes to indicate the status color.
   *
   * @param {string} status - Health status string
   * @returns {HTMLElement} span element with status classes
   */
  function renderStatusBadge(status){
    const cls = 'status-badge status-' + (status || 'UNKNOWN');
    return el('span', { class: cls, text: String(status || 'UNKNOWN') });
  }

  /**
   * formatJSON
   * Safely stringify JSON data for display. Falls back to String() if stringify fails.
   *
   * @param {any} obj - Value to stringify
   * @returns {string} Pretty-printed JSON or fallback string
   */
  function formatJSON(obj){
    try{ return JSON.stringify(obj, null, 2); } catch(e){ return String(obj); }
  }

  /**
   * drawDiskPie
   * Draws a donut-style pie chart on a canvas element representing used vs free disk space.
   * The function handles devicePixelRatio scaling to produce crisp graphics on HiDPI screens.
   * Colors adapt to the current theme (light or dark).
   *
   * @param {HTMLCanvasElement} canvas - Canvas element to draw into
   * @param {number} usedBytes - Number of bytes used on disk
   * @param {number} freeBytes - Number of bytes free on disk
   */
  function drawDiskPie(canvas, usedBytes, freeBytes){
    const ctx = canvas.getContext('2d');
    const ratio = window.devicePixelRatio || 1;
    const width = 180; // CSS pixels
    const height = 180;
    canvas.style.width = width + 'px';
    canvas.style.height = height + 'px';
    canvas.width = Math.round(width * ratio);
    canvas.height = Math.round(height * ratio);
    ctx.scale(ratio, ratio);

    const total = usedBytes + freeBytes;
    const usedPct = total ? (usedBytes / total) : 0;
    const freePct = 1 - usedPct;

    const cx = width / 2;
    const cy = height / 2;
    const radius = Math.min(width, height) / 2 - 8;

    // Detect dark theme for color adaptation
    const isDark = document.documentElement.getAttribute('data-theme') === 'dark';

    // background
    ctx.clearRect(0,0,width,height);

    // draw free slice (green in light mode, brighter in dark mode)
    const start = -Math.PI / 2;
    const usedAngle = usedPct * Math.PI * 2;
    ctx.beginPath();
    ctx.moveTo(cx,cy);
    ctx.arc(cx,cy,radius, start + usedAngle, start + Math.PI * 2);
    ctx.closePath();
    ctx.fillStyle = isDark ? '#5ddc5d' : '#1a7f37'; // green free space
    ctx.fill();

    // draw used slice (red)
    ctx.beginPath();
    ctx.moveTo(cx,cy);
    ctx.arc(cx,cy,radius, start, start + usedAngle);
    ctx.closePath();
    ctx.fillStyle = isDark ? '#ff6b6b' : '#c92a2a'; // red used space
    ctx.fill();

    // inner circle to create donut
    ctx.beginPath();
    ctx.arc(cx,cy,radius*0.55,0,Math.PI*2);
    ctx.fillStyle = isDark ? '#1e1e1e' : '#fff';
    ctx.fill();

    // labels - centered percent
    ctx.fillStyle = isDark ? '#e8e8e8' : '#111';
    ctx.font = '14px system-ui, -apple-system, Roboto, "Helvetica Neue", Arial';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    const percentText = total ? Math.round(usedPct * 100) + '% used' : 'N/A';
    ctx.fillText(percentText, cx, cy);
  }

  /**
   * createComponentRow
   * Creates a DOM row for a named component. The row is focusable and can be toggled to show details.
   * For the special case `diskSpace`, the details area contains a canvas chart (donut) showing used/free.
   *
   * @param {string} name - Component name (e.g., 'diskSpace')
   * @param {Object} comp - Component object from health JSON (may contain status and details)
   * @returns {HTMLElement} Component row element
   */
  function createComponentRow(name, comp){
    const box = el('div', { class: 'component component--clickable', role: 'button', tabindex: '0', 'aria-expanded': 'false' });
    const left = el('div');
    left.appendChild(el('div', { class: 'component__name', text: name }));
    if(comp && comp.details && comp.details.path) left.appendChild(el('div', { class: 'component__meta', text: comp.details.path }));

    const right = el('div', { class: 'component__controls' });
    right.appendChild(renderStatusBadge(comp && comp.status));

    const details = el('div', { class: 'component__details' });

    // for diskSpace, create a canvas-based pie chart and a legend
    if(name === 'diskSpace' && comp && comp.details){
      const total = Number(comp.details.total || 0);
      const free = Number(comp.details.free || 0);
      const used = Math.max(0, total - free);

      const canvasWrap = el('div', { class: 'chart-wrap' });
      const canvas = el('canvas', { class: 'disk-canvas', width: '180', height: '180', 'aria-label': 'Disk usage pie chart' });
      canvasWrap.appendChild(canvas);

      const legend = el('div', { class: 'chart-legend' });
      const usedLabel = el('div', { class: 'small-muted', text: 'Used: ' + Math.round((used/ (total||1))*100) + '% (' + formatBytes(used) + ')' });
      const freeLabel = el('div', { class: 'small-muted', text: 'Free: ' + Math.round((free/ (total||1))*100) + '% (' + formatBytes(free) + ')' });
      legend.appendChild(usedLabel);
      legend.appendChild(freeLabel);

      details.appendChild(canvasWrap);
      details.appendChild(legend);

      // draw immediately when showing
      details._renderChart = () => drawDiskPie(canvas, used, free);
    } else {
      // fallback: show pretty JSON details
      const pre = el('pre', { class: 'component__details pre-block' }, formatJSON(comp && comp.details ? comp.details : comp || {}));
      details.appendChild(pre);
    }

    // toggle expansion
    function toggle(expand){
      // Use a class to track open state which is less brittle than checking inline styles
      const currentlyOpen = box.classList.contains('is-open');
      const shouldOpen = (typeof expand === 'boolean') ? expand : !currentlyOpen;
      if(shouldOpen){
        // Close other open component rows (single-open behavior)
        document.querySelectorAll('.component.is-open').forEach(other => {
          if (other === box) return;
          other.classList.remove('is-open');
          other.setAttribute('aria-expanded', 'false');
          const otherDetails = other.querySelector('.component__details');
          if (otherDetails) otherDetails.style.display = 'none';
        });
        box.classList.add('is-open');
        details.style.display = 'block';
        box.setAttribute('aria-expanded','true');
        if(details._renderChart) details._renderChart();
      } else {
        box.classList.remove('is-open');
        details.style.display = 'none';
        box.setAttribute('aria-expanded','false');
      }
    }

    // clicking the row should toggle (open if closed, close if open)
    box.addEventListener('click', (e) => {
      // ignore clicks that happen inside the details area (so interacting with the chart doesn't close it)
      if (e.target && e.target.closest && e.target.closest('.component__details')) return;
      e.preventDefault();
      toggle();
    });
    box.addEventListener('keydown', (e) => { if(e.key === 'Enter' || e.key === ' ') { e.preventDefault(); toggle(); } });

    box.appendChild(left);
    box.appendChild(right);
    box.appendChild(details);
    return box;
  }

  /**
   * formatBytes
   * Convert raw byte counts into a human-readable string using binary multiples (KB, MB, GB...)
   *
   * @param {number} bytes - Byte count to format
   * @returns {string} Human readable string like '1.2 GB'
   */
  function formatBytes(bytes){
    if(!bytes && bytes !== 0) return 'N/A';
    const units = ['B','KB','MB','GB','TB','PB'];
    let u = 0; let val = Number(bytes);
    while(val >= 1024 && u < units.length-1){ val /= 1024; u++; }
    return val.toFixed(u===0?0:1) + ' ' + units[u];
  }

  /**
   * render
   * Primary render function that builds the entire health UI from the fetched data object.
   * It creates a header with overall status, then a list of components rendered with createComponentRow.
   *
   * @param {Object} data - Parsed health JSON with keys like { status, groups, components }
   */
  function render(data){
    root.innerHTML = '';
    const header = el('div', { class: 'health-hero' });
    header.appendChild(el('h1', { text: 'Application Health' }));
    const statusRow = el('div');
    statusRow.appendChild(el('span', { class: 'small-muted', text: 'Overall status: ' }));
    statusRow.appendChild(renderStatusBadge(data.status));
    header.appendChild(statusRow);
    header.appendChild(el('div', { class: 'small-muted', text: 'Groups: ' + (Array.isArray(data.groups) ? data.groups.join(', ') : '') }));

    const componentsContainer = el('div', { class: 'components' });
    if(data.components && typeof data.components === 'object'){
      Object.keys(data.components).forEach(k => componentsContainer.appendChild(createComponentRow(k, data.components[k])));
    }

    const footer = el('div', { class: 'footer-links' });

    root.appendChild(header);
    root.appendChild(componentsContainer);
    root.appendChild(footer);
  }

  /**
   * showError
   * Display a minimal error UI if the health JSON cannot be loaded.
   * The UI includes a fallback UNKNOWN badge and a link to the Hello consumer.
   *
   * @param {any} err - Error object or message
   */
  function showError(err){
    root.innerHTML = '';
    const msg = el('div', { class: 'health-hero' });
    msg.appendChild(el('h1', { text: 'Application Health' }));
    msg.appendChild(el('div', { class: 'status-badge status-UNKNOWN', text: 'UNKNOWN' }));
    msg.appendChild(el('p', { text: 'Unable to load health data: ' + String(err) }));
    root.appendChild(msg);
  }

  // fetch the /actuator/health
  fetch(API, { credentials: 'same-origin' })
    .then(resp => { if(!resp.ok) throw new Error('HTTP ' + resp.status); return resp.json(); })
    .then(render)
    .catch(showError);

})();
