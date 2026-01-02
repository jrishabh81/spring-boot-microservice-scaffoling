// hello.js - handles interaction on hello.html
//
// Responsibilities:
// - Provide a small UI to call /hello endpoint with an optional name
// - Show a transient status message and keep a history of responses in the result area
// - Render history entries with timestamp and duration
//
// Notes: functions are documented below with short descriptions of their behavior.
(function(){
  'use strict';

  /**
   * DOM query helper (single selector)
   * @param {string} selector - CSS selector passed to document.querySelector
   * @returns {Element|null} the first matching Element or null
   */
  const $ = selector => document.querySelector(selector);
  const nameInput = $('#nameInput');
  const callBtn = $('#callBtn');
  const clearBtn = $('#clearBtn');
  const resultDiv = $('#resultDiv');
  const statusArea = $('#statusArea');

  /**
   * showSpinner
   * Shows a small inline spinner inside the status area to indicate an in-flight request.
   */
  function showSpinner(){ statusArea.innerHTML = '<span class="spinner" aria-hidden="true"></span>'; }

  /**
   * hideSpinner
   * Clears the spinner from the status area.
   */
  function hideSpinner(){ statusArea.innerHTML = ''; }

  /**
   * setResult
   * Writes a short status message into the top of the result area. Preserves and isolates the message
   * from the historical entries rendered below. Also marks the message with a CSS class based on
   * `type` (e.g., 'error' or 'success').
   *
   * @param {string|object} content - Text or object to display; objects are JSON-stringified
   * @param {string} [type] - Optional type hint used to add CSS classes: 'error' | 'success' | 'info'
   */
  function setResult(content, type){
    hideSpinner();
    let statusMsg = resultDiv.querySelector('.status-message');
    if(!statusMsg){
      statusMsg = document.createElement('div');
      statusMsg.className = 'status-message';
      // ensure the status message sits above history
      resultDiv.insertBefore(statusMsg, resultDiv.firstChild);
    }
    statusMsg.className = 'status-message';
    if(type === 'error') statusMsg.classList.add('error');
    else if(type === 'success') statusMsg.classList.add('success');
    if(typeof content === 'string') statusMsg.textContent = content;
    else {
      try { statusMsg.textContent = JSON.stringify(content, null, 2); }
      catch(e){ statusMsg.textContent = String(content); }
    }
  }

  // history stack (latest first)
  const historyStack = [];

  /**
   * renderHistoryEntry
   * Creates a DOM node representing a single history entry.
   * The node contains the response body on the left and meta information (duration, timestamp)
   * on the right. The output is not inserted into the document by this function; it just returns the node.
   *
   * @param {Object} entry - { payload, type, duration, timestamp }
   * @returns {HTMLElement} wrapper element for the history entry
   */
  function renderHistoryEntry(entry){
    const wrapper = document.createElement('div');
    wrapper.className = 'history-entry ' + (entry.type || '');
    // left: response body
    const body = document.createElement('div');
    body.className = 'entry-body';
    const pre = document.createElement('pre');
    pre.className = 'entry-pre';
    if(typeof entry.payload === 'string') pre.textContent = entry.payload;
    else {
      try { pre.textContent = JSON.stringify(entry.payload, null, 2); }
      catch(e){ pre.textContent = String(entry.payload); }
    }
    body.appendChild(pre);

    // right: meta (duration)
    const meta = document.createElement('div');
    meta.className = 'entry-meta';
    const dur = document.createElement('div');
    dur.className = 'entry-duration';
    dur.textContent = (entry.duration != null ? (entry.duration + ' ms') : 'n/a');
    meta.appendChild(dur);

    // optionally show timestamp (small)
    const ts = document.createElement('div');
    ts.className = 'entry-ts';
    ts.textContent = new Date(entry.timestamp).toLocaleTimeString();
    meta.appendChild(ts);

    wrapper.appendChild(body);
    wrapper.appendChild(meta);
    return wrapper;
  }

  /**
   * appendHistory
   * Adds a new history entry to the in-memory history stack and inserts its DOM node at the top
   * of the visible result area (immediately after the status message if present).
   *
   * @param {string|object} payload - The response body or error message
   * @param {string} type - 'success' | 'error' used as a CSS class
   * @param {number|null} duration - Round-trip duration in milliseconds
   */
  function appendHistory(payload, type, duration){
    const entry = { payload, type, duration: duration == null ? null : Math.round(duration), timestamp: Date.now() };
    historyStack.unshift(entry);
    // insert as first child after status message (if present)
    const statusMsg = resultDiv.querySelector('.status-message');
    const node = renderHistoryEntry(entry);
    if(statusMsg) resultDiv.insertBefore(node, statusMsg.nextSibling);
    else resultDiv.insertBefore(node, resultDiv.firstChild);
  }

  /**
   * clearHistory
   * Clears the in-memory history and removes all entries from the DOM result area.
   */
  function clearHistory(){
    // remove all history entries and status message
    historyStack.length = 0;
    resultDiv.innerHTML = '';
  }

  /**
   * callHello
   * Main function that calls the server-side `/hello` endpoint with an optional name query parameter.
   * It measures duration, handles JSON/text responses, stores the result in history, and updates the status UI.
   */
  function callHello(){
    const name = nameInput.value.trim();
    setResult('Loading...', 'info');
    showSpinner();
    const url = '/hello' + (name ? ('?name=' + encodeURIComponent(name)) : '');

    const start = (typeof performance !== 'undefined' && performance.now) ? performance.now() : Date.now();
    fetch(url, { method:'GET', credentials:'same-origin' })
      .then(resp => {
        const contentType = resp.headers.get('content-type') || '';
        if(!resp.ok) return resp.text().then(body => Promise.reject({ status: resp.status, statusText: resp.statusText, body }));
        if(contentType.includes('application/json')) return resp.json();
        return resp.text();
      })
      .then(payload => {
        const duration = ((typeof performance !== 'undefined' && performance.now) ? performance.now() : Date.now()) - start;
        hideSpinner();
        setResult('Last call: success', 'success');
        appendHistory(payload, 'success', duration);
      })
      .catch(err => {
        const duration = ((typeof performance !== 'undefined' && performance.now) ? performance.now() : Date.now()) - start;
        hideSpinner();
        let errMsg;
        if(err && typeof err === 'object' && 'status' in err)
          errMsg = 'Error ' + err.status + ' - ' + (err.statusText || '') + '\n' + (err.body || '');
        else
          errMsg = 'Error: ' + (err && err.message ? err.message : String(err));
        setResult('Last call: error', 'error');
        appendHistory(errMsg, 'error', duration);
      });
  }

  // wire up UI events
  callBtn.addEventListener('click', callHello);
  clearBtn.addEventListener('click', () => { nameInput.value = ''; clearHistory(); setResult('Ready (click Call or press Enter)', 'info'); nameInput.focus(); });
  nameInput.addEventListener('keydown', (e) => { if(e.key === 'Enter'){ e.preventDefault(); callHello(); } });

  // initial hint
  setResult('Ready (click Call or press Enter)', 'info');

})();
