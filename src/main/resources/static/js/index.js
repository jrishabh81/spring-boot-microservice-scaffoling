// index.js - renders a user-friendly health landing page using sampleResponse.json
(function(){
  'use strict';

  const root = document.getElementById('healthRoot');
  const API = '/actuator/health'; // using provided file

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

  function renderStatusBadge(status){
    const cls = 'status-badge status-' + (status || 'UNKNOWN');
    return el('span', { class: cls, text: String(status || 'UNKNOWN') });
  }

  function formatJSON(obj){
    try{ return JSON.stringify(obj, null, 2); } catch(e){ return String(obj); }
  }

  // Add helpers for disk chart & bytes formatting
  function formatBytes(bytes){
    if(bytes === undefined || bytes === null) return 'n/a';
    const units = ['B','KB','MB','GB','TB','PB'];
    let v = Number(bytes);
    if(Number.isNaN(v)) return String(bytes);
    let i = 0;
    while(v >= 1024 && i < units.length-1){ v /= 1024; i++; }
    return v % 1 === 0 ? `${v}${units[i]}` : `${v.toFixed(1)}${units[i]}`;
  }

  // returns an element containing an SVG "pie" and a small legend
  function createDiskChart(details, small){
    const total = Number(details && details.total) || 0;
    const free = Number(details && details.free) || 0;
    const used = Math.max(0, total - free);
    const pct = total > 0 ? Math.round((used / total) * 100) : 0;

    const size = small ? 64 : 96;
    const stroke = small ? 10 : 12;
    const r = (size / 2) - (stroke / 2);
    const cx = size / 2;
    const cy = size / 2;
    const circumference = 2 * Math.PI * r;
    const usedOffset = Math.round(circumference * (1 - (pct / 100)));

    const svgNS = 'http://www.w3.org/2000/svg';
    const wrap = document.createElement('div');
    wrap.className = 'chart-wrap';

    const svg = document.createElementNS(svgNS, 'svg');
    svg.setAttribute('width', String(size));
    svg.setAttribute('height', String(size));
    svg.classList.add('chart-svg');

    // background circle
    const bg = document.createElementNS(svgNS, 'circle');
    bg.setAttribute('cx', cx);
    bg.setAttribute('cy', cy);
    bg.setAttribute('r', r);
    bg.setAttribute('fill', 'none');
    bg.setAttribute('stroke', '#eee');
    bg.setAttribute('stroke-width', String(stroke));

    // foreground (used)
    const fg = document.createElementNS(svgNS, 'circle');
    fg.setAttribute('cx', cx);
    fg.setAttribute('cy', cy);
    fg.setAttribute('r', r);
    fg.setAttribute('fill', 'none');
    fg.setAttribute('stroke', 'var(--accent)');
    fg.setAttribute('stroke-width', String(stroke));
    fg.setAttribute('stroke-linecap', 'round');
    fg.setAttribute('transform', `rotate(-90 ${cx} ${cy})`);
    fg.setAttribute('stroke-dasharray', String(circumference));
    fg.setAttribute('stroke-dashoffset', String(usedOffset));

    svg.appendChild(bg);
    svg.appendChild(fg);

    const legend = document.createElement('div');
    legend.className = 'chart-legend';
    const pctEl = document.createElement('div');
    pctEl.className = 'chart-pct';
    pctEl.textContent = pct + '% used';
    const bytesEl = document.createElement('div');
    bytesEl.className = 'chart-bytes';
    bytesEl.textContent = `${formatBytes(used)} used / ${formatBytes(total)}`;

    legend.appendChild(pctEl);
    legend.appendChild(bytesEl);

    wrap.appendChild(svg);
    wrap.appendChild(legend);
    return wrap;
  }

  // override/create createComponentRow to add inline chart for diskSpace component
  function createComponentRow(name, comp){
    const box = el('div', { class: 'component' });
    const left = el('div');
    left.appendChild(el('div', { class: 'component__name', text: name }));
    left.appendChild(el('div', { class: 'component__meta', text: comp && comp.details ? 'has details' : '' }));

    // if this is diskSpace show a small chart inline
    if(name === 'diskSpace' && comp && comp.details){
      left.appendChild(createDiskChart(comp.details, true));
    }

    const right = el('div', { class: 'component__controls' });
    right.appendChild(renderStatusBadge(comp && comp.status));

    const detailsBtn = el('button', { class: 'toggle-btn', 'aria-expanded': 'false', type: 'button', text: 'Details' });
    const details = el('pre', { class: 'component__details' }, formatJSON(comp && comp.details ? comp.details : comp || {}));

    detailsBtn.addEventListener('click', () => {
      const expanded = details.style.display === 'block';
      details.style.display = expanded ? 'none' : 'block';
      detailsBtn.setAttribute('aria-expanded', (!expanded).toString());
    });

    right.appendChild(detailsBtn);
    box.appendChild(left);
    box.appendChild(right);
    box.appendChild(details);
    return box;
  }

  // render function - integrate disk chart into header
  function render(data){
    root.innerHTML = '';
    const header = el('div', { class: 'health-hero' });
    header.appendChild(el('h1', { text: 'Application Health' }));
    header.appendChild(el('div', { class: 'small-muted', text: 'Overall status: ' }));
    header.appendChild(renderStatusBadge(data.status));
    header.appendChild(el('div', { class: 'small-muted', text: 'Groups: ' + (Array.isArray(data.groups) ? data.groups.join(', ') : '') }));

    const componentsContainer = el('div', { class: 'components' });
    if(data.components && typeof data.components === 'object'){
      Object.keys(data.components).forEach(k => componentsContainer.appendChild(createComponentRow(k, data.components[k])));
    }

    const footer = el('div', { class: 'footer-links' });
    footer.appendChild(el('a', { href: '/hello.html', text: 'Go to Hello consumer' }));

    root.appendChild(header);
    root.appendChild(componentsContainer);
    root.appendChild(footer);
  }

  function showError(err){
    root.innerHTML = '';
    const msg = el('div', { class: 'health-hero' });
    msg.appendChild(el('h1', { text: 'Application Health' }));
    msg.appendChild(el('div', { class: 'status-badge status-UNKNOWN', text: 'UNKNOWN' }));
    msg.appendChild(el('p', { text: 'Unable to load health data: ' + String(err) }));
    msg.appendChild(el('div', { class: 'footer-links' }, el('a', { href: '/hello.html', text: 'Go to Hello consumer' })));
    root.appendChild(msg);
  }

  // fetch the sampleResponse.json
  fetch(API, { credentials: 'same-origin' })
    .then(resp => { if(!resp.ok) throw new Error('HTTP ' + resp.status); return resp.json(); })
    .then(render)
    .catch(showError);

})();
