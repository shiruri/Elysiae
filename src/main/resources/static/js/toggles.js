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
      var isDark = document.documentElement.classList.toggle('dark');
      localStorage.setItem(key, isDark ? 'dark' : 'light');
      btn.classList.remove('toggling');
      void btn.offsetWidth;
      btn.classList.add('toggling');
    });
  });
})();
