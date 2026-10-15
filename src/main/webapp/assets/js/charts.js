(function () {
    'use strict';

    document.addEventListener('DOMContentLoaded', function () {
        var canvases = document.querySelectorAll('canvas[data-chart]');
        canvases.forEach(function (canvas) {
            var rawJson = canvas.getAttribute('data-chart');
            if (!rawJson || !rawJson.trim()) {
                showNoData(canvas);
                return;
            }
            try {
                var config = JSON.parse(rawJson);
                if (!config || !config.data || !config.data.datasets || config.data.datasets.length === 0) {
                    showNoData(canvas);
                    return;
                }
                var firstDataset = config.data.datasets[0];
                if (!firstDataset.data || firstDataset.data.length === 0) {
                    showNoData(canvas);
                    return;
                }

                if (typeof Chart === 'undefined') {
                    console.error('Chart.js is not loaded');
                    return;
                }

                new Chart(canvas, config);
            } catch (err) {
                console.warn('Unable to parse chart configuration', err);
                showNoData(canvas);
            }
        });

        function showNoData(canvas) {
            var p = document.createElement('p');
            p.className = 'text-muted small text-center my-3';
            p.textContent = 'No data yet';
            if (canvas.parentNode) {
                canvas.parentNode.replaceChild(p, canvas);
            }
        }
    });
})();
