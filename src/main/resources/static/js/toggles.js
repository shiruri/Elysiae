(function() {
  var key = 'elysiae-theme';
  var st = localStorage.getItem(key);
  var pd = window.matchMedia('(prefers-color-scheme:dark)').matches;
  if (st === 'dark' || (!st && pd)) {
    document.documentElement.classList.add('dark');
  }

  document.addEventListener('DOMContentLoaded', function() {
    var btn = document.getElementById('theme-toggle');
    if (!btn) return;
    btn.addEventListener('click', function() {
      var wasDark = document.documentElement.classList.contains('dark');
      document.documentElement.classList.add('theme-transitioning');
      document.documentElement.classList.toggle('dark');
      localStorage.setItem(key, document.documentElement.classList.contains('dark') ? 'dark' : 'light');
      btn.classList.remove('toggling');
      void btn.offsetWidth;
      btn.classList.add('toggling');
      setTimeout(function() {
        document.documentElement.classList.remove('theme-transitioning');
      }, 400);
    });
  });
})();
