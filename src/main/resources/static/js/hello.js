// hello.js - handles interaction on hello.html
(function(){
  'use strict';

  const $ = selector => document.querySelector(selector);
  const nameInput = $('#nameInput');
  const callBtn = $('#callBtn');
  const clearBtn = $('#clearBtn');
  const resultDiv = $('#resultDiv');
  const statusArea = $('#statusArea');

  function showSpinner(){ statusArea.innerHTML = '<span class="spinner" aria-hidden="true"></span>'; }
  function hideSpinner(){ statusArea.innerHTML = ''; }

  function setResult(content, type){
    hideSpinner();
    resultDiv.className = '';
    if(type === 'error') resultDiv.classList.add('error');
    else if(type === 'success') resultDiv.classList.add('success');

    if(typeof content === 'string') resultDiv.textContent = content;
    else {
      try { resultDiv.textContent = JSON.stringify(content, null, 2); }
      catch(e){ resultDiv.textContent = String(content); }
    }
  }

  function callHello(){
    const name = nameInput.value.trim();
    setResult('Loading...', 'info');
    showSpinner();
    const url = '/hello' + (name ? ('?name=' + encodeURIComponent(name)) : '');

    fetch(url, { method:'GET', credentials:'same-origin' })
      .then(resp => {
        const contentType = resp.headers.get('content-type') || '';
        if(!resp.ok) return resp.text().then(body => Promise.reject({ status: resp.status, statusText: resp.statusText, body }));
        if(contentType.includes('application/json')) return resp.json();
        return resp.text();
      })
      .then(payload => setResult(payload, 'success'))
      .catch(err => {
        hideSpinner();
        if(err && typeof err === 'object' && 'status' in err)
          setResult('Error ' + err.status + ' - ' + (err.statusText || '') + '\n' + (err.body || ''), 'error');
        else
          setResult('Error: ' + (err && err.message ? err.message : String(err)), 'error');
      });
  }

  callBtn.addEventListener('click', callHello);
  clearBtn.addEventListener('click', () => { nameInput.value = ''; setResult(''); nameInput.focus(); });
  nameInput.addEventListener('keydown', (e) => { if(e.key === 'Enter'){ e.preventDefault(); callHello(); } });

  // initial hint
  setResult('Ready (click Call or press Enter)', 'info');
})();

