const API_BASE_URL = 'http://localhost:8080/api/v1';

document.addEventListener('DOMContentLoaded', () => {
    const token = localStorage.getItem('jwtToken');
    if (token) {
        showDashboard();
    }

    // Login Formu Listener
    document.getElementById('loginForm')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const email = document.getElementById('loginEmail').value;
        const password = document.getElementById('loginPassword').value;

        try {
            const response = await fetch(`${API_BASE_URL}/users/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, password })
            });

            if (response.ok) {
                const data = await response.json();
                localStorage.setItem('jwtToken', data.token);
                showDashboard();
            } else {
                showAlert('authAlert', 'Giriş başarısız! Bilgilerinizi kontrol edin.', 'bg-red-100 text-red-700');
            }
        } catch (error) {
            showAlert('authAlert', 'Sunucuya bağlanılamadı!', 'bg-red-100 text-red-700');
        }
    });

    // Çıkış Butonu
    document.getElementById('logoutBtn')?.addEventListener('click', () => {
        localStorage.removeItem('jwtToken');
        window.location.reload();
    });

    // Transfer Formu Listener
    document.getElementById('transferForm')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const token = localStorage.getItem('jwtToken');
        const fromIban = document.getElementById('fromIban').value;
        const toIban = document.getElementById('toIban').value;
        const amount = document.getElementById('transferAmount').value;

        try {
            const response = await fetch(`${API_BASE_URL}/wallets/transfer`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({ fromIban, toIban, amount })
            });

            if (response.ok) {
                showAlert('transferAlert', 'Transfer başarıyla gerçekleşti!', 'bg-green-100 text-green-700');
                loadWalletData();
                loadTransactions();
            } else {
                const errText = await response.text();
                showAlert('transferAlert', `Hata: ${errText}`, 'bg-red-100 text-red-700');
            }
        } catch (error) {
            showAlert('transferAlert', 'İşlem sırasında bir hata oluştu.', 'bg-red-100 text-red-700');
        }
    });
});

function showDashboard() {
    document.getElementById('authSection').classList.add('hidden');
    document.getElementById('dashboardSection').classList.remove('hidden');
    loadWalletData();
    loadTransactions();
}

async function loadWalletData() {
    const token = localStorage.getItem('jwtToken');
    try {
        const response = await fetch(`${API_BASE_URL}/wallets/me`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (response.ok) {
            const wallet = await response.json();
            document.getElementById('walletBalance').innerText = `${wallet.balance} ${wallet.currency}`;
            document.getElementById('walletIban').innerText = wallet.iban;
            document.getElementById('fromIban').value = wallet.iban;
        }
    } catch (e) { console.error(e); }
}

async function loadTransactions() {
    const token = localStorage.getItem('jwtToken');
    try {
        const response = await fetch(`${API_BASE_URL}/transactions/search?page=0&size=10`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (response.ok) {
            const data = await response.json();
            const tbody = document.getElementById('transactionTableBody');
            tbody.innerHTML = '';

            data.content.forEach(tx => {
                const tr = document.createElement('tr');
                tr.className = "border-b hover:bg-gray-50";
                tr.innerHTML = `
                    <td class="p-3">${new Date(tx.createdAt).toLocaleString('tr-TR')}</td>
                    <td class="p-3 font-mono text-xs">${tx.fromIban}</td>
                    <td class="p-3 font-mono text-xs">${tx.toIban}</td>
                    <td class="p-3 font-bold">${tx.amount} ${tx.currency}</td>
                    <td class="p-3">
                        <button onclick="downloadPdf(${tx.id})" class="text-indigo-600 hover:text-indigo-900 font-semibold text-xs">
                            <i class="fa-solid fa-file-pdf"></i> Dekont İndir
                        </button>
                    </td>
                `;
                tbody.appendChild(tr);
            });
        }
    } catch (e) { console.error(e); }
}

async function downloadPdf(id) {
    const token = localStorage.getItem('jwtToken');
    const response = await fetch(`${API_BASE_URL}/transactions/${id}/pdf`, {
        headers: { 'Authorization': `Bearer ${token}` }
    });
    if(response.ok) {
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `dekont_${id}.pdf`;
        a.click();
    }
}

function showAlert(elementId, message, bgClass) {
    const el = document.getElementById(elementId);
    el.className = `p-3 text-sm rounded-lg ${bgClass}`;
    el.innerText = message;
    el.classList.remove('hidden');
    setTimeout(() => el.classList.add('hidden'), 5000);
}