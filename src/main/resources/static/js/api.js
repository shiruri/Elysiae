(function() {
  'use strict';

  function extractError(data) {
    if (!data) return '';
    return data.message || data.error || data.msg || '';
  }

  var defaultBase = location.port === '8080' ? '' : location.protocol + '//' + location.hostname + ':8080';
  window.API_BASE = window.API_BASE || defaultBase;
  console.log('[API] BASE:', window.API_BASE);

  window.API = {
    BASE: window.API_BASE,
    async request(method, path, body) {
      const token = localStorage.getItem('elysiae-token');
      const headers = { 'Content-Type': 'application/json' };
      if (token) headers['Authorization'] = 'Bearer ' + token;
      const opts = { method: method, headers: headers };
      if (body !== undefined) opts.body = JSON.stringify(body);
      const res = await fetch(this.BASE + path, opts);
      var isLoginReq = path === '/api/auth/login';

      if (res.status === 401) {
        if (isLoginReq) {
          const data = await res.json().catch(function() { return {}; });
          throw new Error('Invalid username or password.');
        }
        localStorage.removeItem('elysiae-token');
        localStorage.removeItem('elysiae-user');
        window.location.href = 'login.html';
        throw new Error('Your session has expired. Please sign in again.');
      }
      if (res.status === 403) {
        const data = await res.json().catch(function() { return {}; });
        if (data.error === 'PASSWORD_CHANGE_REQUIRED') {
          window.location.href = 'login.html?changePassword=1';
          throw new Error('You must change your password before continuing.');
        }
        throw new Error(extractError(data) || 'You do not have permission to perform this action.');
      }
      if (!res.ok) {
        const data = await res.json().catch(function() { return {}; });
        var msg = extractError(data);
        if (isLoginReq) throw new Error('Invalid username or password.');
        if (msg) throw new Error(msg);
        if (res.status === 404) throw new Error('The requested resource was not found.');
        if (res.status === 409) throw new Error('A conflict occurred. The record may already exist.');
        if (res.status === 422) throw new Error('The submitted data is invalid.');
        if (res.status >= 500) throw new Error('A server error occurred. Please try again later.');
        throw new Error('Request failed (' + res.status + '). Please try again.');
      }
      if (res.status === 204) return null;
      const text = await res.text();
      if (!text) return null;
      try { return JSON.parse(text); } catch (e) { return text; }
    },
    get: function(path) { return this.request('GET', path); },
    post: function(path, body) { return this.request('POST', path, body); },
    patch: function(path, body) { return this.request('PATCH', path, body); },
    del: function(path) { return this.request('DELETE', path); }
  };
})();
