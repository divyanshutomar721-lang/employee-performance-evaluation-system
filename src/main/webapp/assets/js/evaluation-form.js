/**
 * Live preview calculation for employee performance evaluation form.
 *
 * Reads selected ratings and criteria weights from data attributes,
 * calculating the projected overall score and Rocket Stage tier using
 * the exact mathematical formula and threshold bands as the server:
 *
 *   overall = (sum(rating * weight) / (5 * sum(weight))) * 100
 *
 * Threshold bands:
 *   < 40.00       -> Launchpad
 *   40.00 - 59.99 -> Liftoff
 *   60.00 - 74.99 -> Orbit
 *   75.00 - 89.99 -> Cruise
 *   >= 90.00      -> Summit
 *
 * Notice: This is a client-side preview only; final score is calculated on save.
 */
document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('evaluation-form');
    if (!form) return;

    const scoreDisplay = document.getElementById('preview-score');
    const stageDisplay = document.getElementById('preview-stage');
    const previewContainer = document.getElementById('live-score-preview');

    function calculatePreview() {
        const blocks = document.querySelectorAll('.criterion-block');
        let totalWeightedRating = 0;
        let totalWeight = 0;
        let ratedCount = 0;

        blocks.forEach(function (block) {
            const weight = parseFloat(block.getAttribute('data-weight')) || 0;
            const checkedRadio = block.querySelector('input[type="radio"]:checked');

            if (checkedRadio && checkedRadio.value) {
                const rating = parseInt(checkedRadio.value, 10);
                if (rating >= 1 && rating <= 5) {
                    totalWeightedRating += (rating * weight);
                    totalWeight += weight;
                    ratedCount++;
                }
            }
        });

        if (ratedCount === 0 || totalWeight === 0) {
            if (scoreDisplay) scoreDisplay.textContent = '--';
            if (stageDisplay) {
                stageDisplay.textContent = 'None';
                stageDisplay.className = 'badge bg-secondary';
            }
            return;
        }

        const score = (totalWeightedRating / (5.0 * totalWeight)) * 100.0;
        const formattedScore = score.toFixed(2);

        let stage = 'Launchpad';
        let badgeClass = 'badge stage-launchpad badge-stage-launchpad';

        if (score >= 90.0) {
            stage = 'Summit';
            badgeClass = 'badge stage-summit badge-stage-summit';
        } else if (score >= 75.0) {
            stage = 'Cruise';
            badgeClass = 'badge stage-cruise badge-stage-cruise';
        } else if (score >= 60.0) {
            stage = 'Orbit';
            badgeClass = 'badge stage-orbit badge-stage-orbit';
        } else if (score >= 40.0) {
            stage = 'Liftoff';
            badgeClass = 'badge stage-liftoff badge-stage-liftoff';
        }

        if (scoreDisplay) scoreDisplay.textContent = formattedScore;
        if (stageDisplay) {
            stageDisplay.textContent = stage;
            stageDisplay.className = badgeClass;
        }
    }

    form.addEventListener('change', function (e) {
        if (e.target && e.target.type === 'radio' && e.target.name.startsWith('rating_')) {
            calculatePreview();
        }
    });

    // Run initial preview calculation in case values were pre-filled
    calculatePreview();
});
