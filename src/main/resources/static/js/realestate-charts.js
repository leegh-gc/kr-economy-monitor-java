'use strict';

// ─────────────────────────────────────────────────────────────
// 구역 / 구 코드 매핑
// ─────────────────────────────────────────────────────────────
const ZONES = {
    gangnam: {
        name: '강남권',
        districts: [
            { name: '강남구', code: '11680' },
            { name: '서초구', code: '11650' },
            { name: '송파구', code: '11710' },
        ]
    },
    gangdong: {
        name: '강동권',
        districts: [
            { name: '강동구', code: '11740' },
            { name: '노원구', code: '11350' },
            { name: '성동구', code: '11200' },
        ]
    },
    gangseo: {
        name: '강서권',
        districts: [
            { name: '강서구', code: '11500' },
            { name: '영등포구', code: '11560' },
            { name: '양천구', code: '11470' },
        ]
    },
    gangbuk: {
        name: '강북권',
        districts: [
            { name: '종로구', code: '11110' },
            { name: '마포구', code: '11440' },
            { name: '용산구', code: '11170' },
        ]
    },
};

const COLORS = [
    '#0d6efd', '#dc3545', '#198754', '#fd7e14',
    '#6f42c1', '#0dcaf0', '#ffc107', '#20c997'
];

let currentSaleChart = null;
let currentLeaseChart = null;
let currentKbChart = null;

// ─────────────────────────────────────────────────────────────
// 공통 헬퍼
// ─────────────────────────────────────────────────────────────
async function fetchApi(url) {
    const res = await fetch(url);
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    return res.json();
}

function showEl(id, show = true) {
    const el = document.getElementById(id);
    if (el) el.style.display = show ? 'block' : 'none';
}

function showError(id, msg) {
    const el = document.getElementById(id);
    if (el) el.innerHTML = `<span class="text-danger small"><i class="bi bi-exclamation-circle me-1"></i>${msg}</span>`;
}

function markdownToHtml(md) {
    if (!md) return '';
    return md
        .replace(/^### (.+)$/gm, '<h5 class="mt-3 mb-1 fw-bold">$1</h5>')
        .replace(/^## (.+)$/gm, '<h4 class="mt-3 mb-2 fw-bold">$1</h4>')
        .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
        .replace(/^- (.+)$/gm, '<li>$1</li>')
        .replace(/(<li>.*<\/li>)/gs, '<ul>$1</ul>')
        .replace(/\n\n/g, '</p><p>')
        .replace(/^([^<].+)$/gm, '<p>$1</p>');
}

// ─────────────────────────────────────────────────────────────
// KB 지수 차트
// ─────────────────────────────────────────────────────────────
async function loadKbIndexChart() {
    try {
        const data = await fetchApi('/api/realestate/kb-index');
        showEl('kb-index-loading', false);
        showEl('kbIndexChart', true);

        if (currentKbChart) currentKbChart.destroy();
        const ctx = document.getElementById('kbIndexChart');
        currentKbChart = new Chart(ctx, {
            type: 'line',
            data: {
                datasets: data.series.map((s, i) => ({
                    label: s.name,
                    data: s.data.map(d => ({ x: d.date, y: d.value })),
                    borderColor: COLORS[i],
                    backgroundColor: COLORS[i] + '20',
                    tension: 0.3,
                    pointRadius: 0,
                    borderWidth: 2,
                }))
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                interaction: { mode: 'index', intersect: false },
                plugins: {
                    legend: { position: 'top', labels: { boxWidth: 12 } },
                },
                scales: {
                    x: { type: 'category', ticks: { maxTicksLimit: 12, font: { size: 10 } } },
                    y: { ticks: { font: { size: 10 } } },
                }
            }
        });
    } catch (e) {
        showError('kb-index-loading', 'KB 지수 데이터 로드 실패');
    }
}

// ─────────────────────────────────────────────────────────────
// 구별 통계 차트
// ─────────────────────────────────────────────────────────────
async function loadDistrictStats(sigunguCode) {
    showEl('stats-loading', true);
    showEl('stats-charts', false);

    try {
        const data = await fetchApi(`/api/realestate/stats/${sigunguCode}`);

        showEl('stats-loading', false);
        showEl('stats-charts', true);

        // 매매 차트
        if (currentSaleChart) currentSaleChart.destroy();
        const saleCtx = document.getElementById('saleStatsChart');
        const saleData = (data.sale || []).slice(0, 24).reverse();
        currentSaleChart = new Chart(saleCtx, {
            type: 'line',
            data: {
                datasets: [
                    { label: '최저가', data: saleData.map(d => ({ x: d.dealYymm, y: d.minPrice })), borderColor: COLORS[2], backgroundColor: COLORS[2] + '20', tension: 0.3, pointRadius: 0, borderWidth: 1.5 },
                    { label: '평균가', data: saleData.map(d => ({ x: d.dealYymm, y: d.avgPrice })), borderColor: COLORS[0], backgroundColor: COLORS[0] + '30', tension: 0.3, pointRadius: 2, borderWidth: 2.5, fill: true },
                    { label: '최고가', data: saleData.map(d => ({ x: d.dealYymm, y: d.maxPrice })), borderColor: COLORS[1], backgroundColor: COLORS[1] + '20', tension: 0.3, pointRadius: 0, borderWidth: 1.5 },
                ]
            },
            options: buildStatsOptions('매매가(만원)')
        });

        // 전세 차트
        if (currentLeaseChart) currentLeaseChart.destroy();
        const leaseCtx = document.getElementById('leaseStatsChart');
        const leaseData = (data.lease || []).slice(0, 24).reverse();
        currentLeaseChart = new Chart(leaseCtx, {
            type: 'line',
            data: {
                datasets: [
                    { label: '최저가', data: leaseData.map(d => ({ x: d.dealYymm, y: d.minDeposit })), borderColor: COLORS[2], backgroundColor: COLORS[2] + '20', tension: 0.3, pointRadius: 0, borderWidth: 1.5 },
                    { label: '평균가', data: leaseData.map(d => ({ x: d.dealYymm, y: d.avgDeposit })), borderColor: COLORS[3], backgroundColor: COLORS[3] + '30', tension: 0.3, pointRadius: 2, borderWidth: 2.5, fill: true },
                    { label: '최고가', data: leaseData.map(d => ({ x: d.dealYymm, y: d.maxDeposit })), borderColor: COLORS[1], backgroundColor: COLORS[1] + '20', tension: 0.3, pointRadius: 0, borderWidth: 1.5 },
                ]
            },
            options: buildStatsOptions('전세가(만원)')
        });

    } catch (e) {
        showError('stats-loading', '통계 데이터 로드 실패');
    }
}

function buildStatsOptions(yLabel) {
    return {
        responsive: true,
        maintainAspectRatio: true,
        interaction: { mode: 'index', intersect: false },
        plugins: { legend: { position: 'top', labels: { boxWidth: 10, font: { size: 10 } } } },
        scales: {
            x: { type: 'category', ticks: { maxTicksLimit: 8, font: { size: 9 } } },
            y: { title: { display: true, text: yLabel, font: { size: 10 } }, ticks: { font: { size: 9 } } },
        }
    };
}

// ─────────────────────────────────────────────────────────────
// TOP5 아파트 테이블
// ─────────────────────────────────────────────────────────────
async function loadTop5(sigunguCode) {
    showEl('top5-loading', true);
    showEl('top5-content', false);

    try {
        const data = await fetchApi(`/api/realestate/top-apartments/${sigunguCode}`);
        showEl('top5-loading', false);
        showEl('top5-content', true);

        // 매매 TOP5
        const saleBody = document.getElementById('sale-top5-body');
        if (saleBody) {
            saleBody.innerHTML = (data.sale || []).map(apt => `
                <tr>
                    <td class="fw-semibold">${apt.aptName || '-'}</td>
                    <td>${apt.landDong || '-'}</td>
                    <td>${apt.buildYear || '-'}</td>
                    <td class="text-end text-primary fw-bold">${apt.avgPrice ? Number(apt.avgPrice).toLocaleString() : '-'}</td>
                </tr>
            `).join('') || '<tr><td colspan="4" class="text-center text-muted">데이터 없음</td></tr>';
        }

        // 전세 TOP5
        const leaseBody = document.getElementById('lease-top5-body');
        if (leaseBody) {
            leaseBody.innerHTML = (data.lease || []).map(apt => `
                <tr>
                    <td class="fw-semibold">${apt.aptName || '-'}</td>
                    <td>${apt.landDong || '-'}</td>
                    <td>${apt.buildYear || '-'}</td>
                    <td class="text-end text-warning fw-bold">${apt.avgDeposit ? Number(apt.avgDeposit).toLocaleString() : '-'}</td>
                </tr>
            `).join('') || '<tr><td colspan="4" class="text-center text-muted">데이터 없음</td></tr>';
        }
    } catch (e) {
        showError('top5-loading', 'TOP5 데이터 로드 실패');
    }
}

// ─────────────────────────────────────────────────────────────
// 구역 / 구 선택 변경 핸들러
// ─────────────────────────────────────────────────────────────
function onZoneChange() {
    const zoneKey = document.getElementById('zone-select').value;
    const zone = ZONES[zoneKey];
    const districtSelect = document.getElementById('district-select');

    districtSelect.innerHTML = zone.districts.map(d =>
        `<option value="${d.code}">${d.name}</option>`
    ).join('');

    onDistrictChange();
}

function onDistrictChange() {
    const code = document.getElementById('district-select').value;
    loadDistrictStats(code);
    loadTop5(code);
}

// ─────────────────────────────────────────────────────────────
// AI 분석
// ─────────────────────────────────────────────────────────────
async function loadRealestateAnalysis() {
    const loadingEl = document.getElementById('re-analysis-loading');
    const contentEl = document.getElementById('re-analysis-content');
    if (loadingEl) loadingEl.style.display = 'block';
    if (contentEl) contentEl.textContent = '';

    try {
        const data = await fetchApi('/api/realestate/analysis');
        if (contentEl) contentEl.innerHTML = markdownToHtml(data.analysis);
        loadRealestateCartoon();
    } catch (e) {
        if (contentEl) contentEl.innerHTML = '<span class="text-danger">분석 로드 실패</span>';
    } finally {
        if (loadingEl) loadingEl.style.display = 'none';
    }
}

async function loadRealestateCartoon() {
    try {
        const data = await fetchApi('/api/realestate/cartoon');
        if (data.cartoonB64) {
            const img = document.getElementById('re-cartoon-image');
            const placeholder = document.getElementById('re-cartoon-placeholder');
            if (img) {
                img.src = 'data:image/png;base64,' + data.cartoonB64;
                img.style.display = 'block';
            }
            if (placeholder) placeholder.style.display = 'none';
        }
    } catch (e) {
        // 컷툰은 선택사항
    }
}

// ─────────────────────────────────────────────────────────────
// 진입점
// ─────────────────────────────────────────────────────────────
function initRealestatePage() {
    // 구 선택 드롭다운 초기화
    onZoneChange();
    // KB 지수 로드
    loadKbIndexChart();
}
