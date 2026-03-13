'use strict';

// ─────────────────────────────────────────────────────────────
// 공통 헬퍼
// ─────────────────────────────────────────────────────────────
const COLORS = [
    '#0d6efd', '#dc3545', '#198754', '#fd7e14',
    '#6f42c1', '#0dcaf0', '#ffc107', '#20c997'
];

function showChart(loadingId, canvasId) {
    const loading = document.getElementById(loadingId);
    const canvas = document.getElementById(canvasId);
    if (loading) loading.style.display = 'none';
    if (canvas) canvas.style.display = 'block';
}

function showError(loadingId, message) {
    const el = document.getElementById(loadingId);
    if (el) el.innerHTML = `<span class="text-danger small"><i class="bi bi-exclamation-circle me-1"></i>${message}</span>`;
}

async function fetchApi(url) {
    const res = await fetch(url);
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    return res.json();
}

function buildLineDatasets(series) {
    return series.map((s, i) => ({
        label: s.name,
        data: s.data.map(d => ({ x: d.date, y: d.value })),
        borderColor: COLORS[i % COLORS.length],
        backgroundColor: COLORS[i % COLORS.length] + '20',
        tension: 0.3,
        pointRadius: 0,
        borderWidth: 2,
    }));
}

function buildBarDatasets(series) {
    return series.map((s, i) => ({
        label: s.name,
        data: s.data.map(d => ({ x: d.date, y: d.value })),
        backgroundColor: COLORS[i % COLORS.length] + 'cc',
        borderColor: COLORS[i % COLORS.length],
        borderWidth: 1,
    }));
}

const commonOptions = {
    responsive: true,
    maintainAspectRatio: true,
    interaction: { mode: 'index', intersect: false },
    plugins: {
        legend: { position: 'top', labels: { boxWidth: 12, font: { size: 11 } } },
        tooltip: { mode: 'index', intersect: false },
    },
    scales: {
        x: {
            type: 'category',
            ticks: { maxRotation: 0, maxTicksLimit: 8, font: { size: 10 } },
        },
        y: { ticks: { font: { size: 10 } } },
    },
};

function createChart(canvasId, type, datasets, options = {}) {
    const ctx = document.getElementById(canvasId);
    if (!ctx) return null;
    return new Chart(ctx, {
        type,
        data: { datasets },
        options: { ...commonOptions, ...options },
    });
}

// ─────────────────────────────────────────────────────────────
// 각 섹션 차트 로드 함수
// ─────────────────────────────────────────────────────────────

async function loadInterestRateChart() {
    try {
        const data = await fetchApi('/api/economy/interest-rate');
        showChart('interest-rate-loading', 'interestRateChart');
        createChart('interestRateChart', 'line', buildLineDatasets(data.series));
    } catch (e) {
        showError('interest-rate-loading', '금리 데이터 로드 실패');
    }
}

async function loadGdpChart() {
    try {
        const data = await fetchApi('/api/economy/gdp');
        showChart('gdp-loading', 'gdpChart');
        // GDP 성장률(%)은 좌축, 1인당 GDP(달러)는 우축
        const datasets = data.series.map((s, i) => ({
            label: s.name,
            data: s.data.map(d => ({ x: d.date, y: d.value })),
            type: i === 0 ? 'bar' : 'line',
            borderColor: COLORS[i],
            backgroundColor: i === 0 ? COLORS[0] + '99' : COLORS[1] + '20',
            yAxisID: i === 0 ? 'y' : 'y1',
            tension: 0.3,
            pointRadius: i === 0 ? 0 : 3,
            borderWidth: 2,
        }));
        createChart('gdpChart', 'bar', datasets, {
            scales: {
                ...commonOptions.scales,
                y: { type: 'linear', position: 'left', title: { display: true, text: 'GDP 성장률(%)' } },
                y1: { type: 'linear', position: 'right', title: { display: true, text: '1인당 GDP(달러)' }, grid: { drawOnChartArea: false } },
            }
        });
    } catch (e) {
        showError('gdp-loading', 'GDP 데이터 로드 실패');
    }
}

async function loadExchangeRateChart() {
    try {
        const data = await fetchApi('/api/economy/exchange-rate');
        showChart('exchange-rate-loading', 'exchangeRateChart');
        // JPY는 우측 Y축
        const datasets = data.series.map((s, i) => {
            const isJpy = s.name.includes('JPY') || s.name.includes('엔');
            return {
                label: s.name,
                data: s.data.map(d => ({ x: d.date, y: d.value })),
                borderColor: COLORS[i],
                backgroundColor: COLORS[i] + '20',
                tension: 0.3,
                pointRadius: 0,
                borderWidth: 2,
                yAxisID: isJpy ? 'y1' : 'y',
            };
        });
        createChart('exchangeRateChart', 'line', datasets, {
            scales: {
                ...commonOptions.scales,
                y: { type: 'linear', position: 'left', title: { display: true, text: '원화 환율' } },
                y1: { type: 'linear', position: 'right', title: { display: true, text: '원/100엔(JPY)' }, grid: { drawOnChartArea: false } },
            }
        });
    } catch (e) {
        showError('exchange-rate-loading', '환율 데이터 로드 실패');
    }
}

async function loadPriceIndexChart() {
    try {
        const data = await fetchApi('/api/economy/price-index');
        showChart('price-index-loading', 'priceIndexChart');
        createChart('priceIndexChart', 'line', buildLineDatasets(data.series));
    } catch (e) {
        showError('price-index-loading', '물가 데이터 로드 실패');
    }
}

async function loadTradeChart() {
    try {
        const data = await fetchApi('/api/economy/trade');
        showChart('trade-loading', 'tradeChart');
        createChart('tradeChart', 'bar', buildBarDatasets(data.series));
    } catch (e) {
        showError('trade-loading', '무역 데이터 로드 실패');
    }
}

async function loadEmploymentChart() {
    try {
        const data = await fetchApi('/api/economy/employment');
        showChart('employment-loading', 'employmentChart');
        const datasets = data.series.map((s, i) => ({
            label: s.name,
            data: s.data.map(d => ({ x: d.date, y: d.value })),
            borderColor: COLORS[i],
            backgroundColor: COLORS[i] + '20',
            tension: 0.3,
            pointRadius: 0,
            borderWidth: 2,
            yAxisID: i === 0 ? 'y' : 'y1',
        }));
        createChart('employmentChart', 'line', datasets, {
            scales: {
                ...commonOptions.scales,
                y: { type: 'linear', position: 'left', title: { display: true, text: '실업률(%)' } },
                y1: { type: 'linear', position: 'right', title: { display: true, text: '취업자수(천명)' }, grid: { drawOnChartArea: false } },
            }
        });
    } catch (e) {
        showError('employment-loading', '고용 데이터 로드 실패');
    }
}

async function loadMoneySupplyChart() {
    try {
        const data = await fetchApi('/api/economy/money-supply');
        showChart('money-supply-loading', 'moneySupplyChart');
        createChart('moneySupplyChart', 'line', buildLineDatasets(data.series));
    } catch (e) {
        showError('money-supply-loading', '통화량 데이터 로드 실패');
    }
}

async function loadPopulationChart() {
    try {
        const data = await fetchApi('/api/economy/population');
        showChart('population-loading', 'populationChart');
        // 인구(좌), 출산율/고령비율(우)
        const datasets = data.series.map((s, i) => {
            const isRight = s.name.includes('비율') || s.name.includes('출산율');
            return {
                label: s.name,
                data: s.data.map(d => ({ x: d.date, y: d.value })),
                borderColor: COLORS[i],
                backgroundColor: COLORS[i] + '20',
                tension: 0.3,
                pointRadius: 3,
                borderWidth: 2,
                yAxisID: isRight ? 'y1' : 'y',
            };
        });
        createChart('populationChart', 'line', datasets, {
            scales: {
                ...commonOptions.scales,
                y: { type: 'linear', position: 'left', title: { display: true, text: '인구(천명)' } },
                y1: { type: 'linear', position: 'right', title: { display: true, text: '비율(%)' }, grid: { drawOnChartArea: false } },
            }
        });
    } catch (e) {
        showError('population-loading', '인구 데이터 로드 실패');
    }
}

// ─────────────────────────────────────────────────────────────
// AI 분석
// ─────────────────────────────────────────────────────────────

async function loadEconomyAnalysis() {
    const btn = document.getElementById('refresh-analysis-btn');
    const loadingEl = document.getElementById('analysis-loading');
    const contentEl = document.getElementById('analysis-content');
    if (btn) btn.disabled = true;
    if (loadingEl) loadingEl.style.display = 'block';
    if (contentEl) contentEl.textContent = '';

    try {
        const data = await fetchApi('/api/economy/analysis');
        if (contentEl) contentEl.innerHTML = markdownToHtml(data.analysis);
        // 컷툰 로드
        loadEconomyCartoon();
    } catch (e) {
        if (contentEl) contentEl.innerHTML = '<span class="text-danger">분석 로드 실패</span>';
    } finally {
        if (loadingEl) loadingEl.style.display = 'none';
        if (btn) btn.disabled = false;
    }
}

async function loadEconomyCartoon() {
    try {
        const data = await fetchApi('/api/economy/cartoon');
        if (data.cartoonB64) {
            const img = document.getElementById('cartoon-image');
            const placeholder = document.getElementById('cartoon-placeholder');
            if (img) {
                img.src = 'data:image/png;base64,' + data.cartoonB64;
                img.style.display = 'block';
            }
            if (placeholder) placeholder.style.display = 'none';
        }
    } catch (e) {
        // 컷툰은 선택사항, 오류 무시
    }
}

// ─────────────────────────────────────────────────────────────
// 마크다운 → HTML 변환 (간단)
// ─────────────────────────────────────────────────────────────
function markdownToHtml(md) {
    if (!md) return '';
    return md
        .replace(/^### (.+)$/gm, '<h5 class="mt-3 mb-1 fw-bold">$1</h5>')
        .replace(/^## (.+)$/gm, '<h4 class="mt-3 mb-2 fw-bold">$1</h4>')
        .replace(/^# (.+)$/gm, '<h3 class="mt-3 mb-2 fw-bold">$1</h3>')
        .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
        .replace(/\*(.+?)\*/g, '<em>$1</em>')
        .replace(/^- (.+)$/gm, '<li>$1</li>')
        .replace(/(<li>.*<\/li>)/gs, '<ul>$1</ul>')
        .replace(/\n\n/g, '</p><p>')
        .replace(/^(.+)$/gm, (m) => m.startsWith('<') ? m : `<p>${m}</p>`);
}

// ─────────────────────────────────────────────────────────────
// 진입점: 모든 차트 병렬 로드
// ─────────────────────────────────────────────────────────────
function loadAllEconomyCharts() {
    loadInterestRateChart();
    loadGdpChart();
    loadExchangeRateChart();
    loadPriceIndexChart();
    loadTradeChart();
    loadEmploymentChart();
    loadMoneySupplyChart();
    loadPopulationChart();
}
