const API_BASE_URL = 'http://localhost:8080/api/v1';

// Global Cüzdan Hafızası
let allWallets = [];

// --- Dark Mode Başlangıç Kontrolü ---
if (localStorage.getItem('theme') === 'dark') {
    document.documentElement.classList.add('dark');
}

// --- Global Modal ve Yardımcı Fonksiyonlar ---
function openModal(modalId) {
    document.getElementById(modalId)?.classList.remove('hidden');
}

function closeModal(modalId) {
    document.getElementById(modalId)?.classList.add('hidden');
}

function openDepositModal() {
    const currentIban = document.getElementById('walletIban')?.innerText;
    if (currentIban) {
        document.getElementById('depositIban').value = currentIban;
    }
    openModal('depositModal');
}

function openExchangeModal() {
    updateWalletSelector(); // Seçenekleri tazelemek için
    openModal('exchangeModal');
}

function openCreateWalletModal() {
    openModal('createWalletModal');
}

// --- Sekme Değiştirme Fonksiyonu (Global Scope) ---
function switchTab(tabId, element) {
    document.querySelectorAll('.settings-content').forEach(tab => tab.classList.add('hidden'));

    const selectedTab = document.getElementById(tabId);
    if (selectedTab) {
        selectedTab.classList.remove('hidden');
    }

    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.className = "tab-btn w-full flex items-center space-x-3 p-3 rounded-lg text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800 transition";
    });

    const activeBtn = element || (window.event && window.event.currentTarget);
    if (activeBtn) {
        activeBtn.className = "tab-btn w-full flex items-center space-x-3 p-3 rounded-lg bg-indigo-600 text-white shadow-md transition";
    }
}

// --- Cüzdan Yönetimi ve UI Güncelleme Fonksiyonları ---
function updateWalletSelector() {
    const selector = document.getElementById('walletSelector');
    if (selector) {
        selector.innerHTML = allWallets.map(w => `<option value="${w.iban}">${w.currency} Cüzdanı</option>`).join('');
    }

    // Exchange modalındaki dropdownları doldur
    const fromSelect = document.getElementById('exchangeFrom');
    const toSelect = document.getElementById('exchangeTo');
    if (fromSelect && toSelect) {
        const options = allWallets.map(w => `<option value="${w.iban}">${w.currency} (${w.balance.toFixed(2)})</option>`).join('');
        fromSelect.innerHTML = options;
        toSelect.innerHTML = options;
    }
}

function onWalletChange() {
    const selectedIban = document.getElementById('walletSelector').value;
    const wallet = allWallets.find(w => w.iban === selectedIban);
    if (wallet) {
        renderActiveWallet(wallet);
    }
}

function renderActiveWallet(wallet) {
    if (!wallet) return;
    const balanceEl = document.getElementById('walletBalance');
    const ibanEl = document.getElementById('walletIban');
    const fromIbanInput = document.getElementById('fromIban');
    const activeCurrencyEl = document.getElementById('activeCurrency');

    if (balanceEl) balanceEl.innerText = `${wallet.balance.toFixed(2)} ${wallet.currency}`;
    if (ibanEl) ibanEl.innerText = wallet.iban;
    if (fromIbanInput) fromIbanInput.value = wallet.iban;
    if (activeCurrencyEl) activeCurrencyEl.innerText = `${wallet.currency} Bakiye`;
}

document.addEventListener('DOMContentLoaded', () => {
    updateDarkModeUI();

    const token = localStorage.getItem('jwtToken');
    if (token) {
        showDashboard();
    }

    // --- Ayarlar Sekme Geçişleri ---
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const targetTab = btn.getAttribute('data-tab');
            if (targetTab) switchTab(targetTab, btn);
        });
    });

    // --- Dark Mode Toggle Listener ---
    document.getElementById('darkModeToggle')?.addEventListener('click', () => {
        const isDark = document.documentElement.classList.toggle('dark');
        localStorage.setItem('theme', isDark ? 'dark' : 'light');
        updateDarkModeUI();
    });

    // --- Auth Sekme Geçiş Mantığı ---
    document.getElementById('tabLoginBtn')?.addEventListener('click', () => {
        document.getElementById('loginForm')?.classList.remove('hidden');
        document.getElementById('registerForm')?.classList.add('hidden');
        document.getElementById('tabLoginBtn').className = "w-1/2 py-2 text-center font-bold border-b-2 border-indigo-600 text-indigo-600";
        document.getElementById('tabRegisterBtn').className = "w-1/2 py-2 text-center font-bold text-gray-500 hover:text-indigo-600 dark:text-gray-400";
    });

    document.getElementById('tabRegisterBtn')?.addEventListener('click', () => {
        document.getElementById('registerForm')?.classList.remove('hidden');
        document.getElementById('loginForm')?.classList.add('hidden');
        document.getElementById('tabRegisterBtn').className = "w-1/2 py-2 text-center font-bold border-b-2 border-indigo-600 text-indigo-600";
        document.getElementById('tabLoginBtn').className = "w-1/2 py-2 text-center font-bold text-gray-500 hover:text-indigo-600 dark:text-gray-400";
    });

    // --- Kayıt Formu ---
    document.getElementById('registerForm')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const firstName = document.getElementById('regFirstName').value.trim();
        const lastName = document.getElementById('regLastName').value.trim();
        const email = document.getElementById('regEmail').value.trim();
        const password = document.getElementById('regPassword').value;

        try {
            const response = await fetch(`${API_BASE_URL}/users/register`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
                body: JSON.stringify({ firstName, lastName, email, password })
            });

            if (response.ok) {
                const data = await response.json();
                localStorage.setItem('jwtToken', data.token);
                showDashboard();
            } else {
                let errorMessage = 'Bilgileri kontrol edin.';
                try {
                    const errData = await response.json();
                    errorMessage = errData.message || errorMessage;
                } catch (err) {
                    const errText = await response.text();
                    if (errText) errorMessage = errText;
                }
                showAlert('authAlert', `Kayıt Hatası: ${errorMessage}`, 'bg-red-100 text-red-700 dark:bg-red-900/50 dark:text-red-300');
            }
        } catch (error) {
            console.error('Register error:', error);
            showAlert('authAlert', 'Sunucuya bağlanılamadı!', 'bg-red-100 text-red-700 dark:bg-red-900/50 dark:text-red-300');
        }
    });

    // --- Giriş Formu ---
    document.getElementById('loginForm')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const email = document.getElementById('loginEmail').value.trim();
        const password = document.getElementById('loginPassword').value;

        try {
            const response = await fetch(`${API_BASE_URL}/users/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
                body: JSON.stringify({ email, password })
            });

            if (response.ok) {
                const data = await response.json();
                localStorage.setItem('jwtToken', data.token);
                showDashboard();
            } else {
                showAlert('authAlert', 'Giriş başarısız! Bilgilerinizi kontrol edin.', 'bg-red-100 text-red-700 dark:bg-red-900/50 dark:text-red-300');
            }
        } catch (error) {
            console.error('Login error:', error);
            showAlert('authAlert', 'Sunucuya bağlanılamadı!', 'bg-red-100 text-red-700 dark:bg-red-900/50 dark:text-red-300');
        }
    });

    // --- Çıkış Butonu ---
    document.getElementById('logoutBtn')?.addEventListener('click', () => {
        localStorage.removeItem('jwtToken');
        window.location.reload();
    });

    // --- Para Transfer Formu ---
    document.getElementById('transferForm')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const token = localStorage.getItem('jwtToken');
        if (!token) return handleUnauthorized();

        const fromIban = document.getElementById('fromIban').value.trim();
        const toIban = document.getElementById('toIban').value.trim();
        const amount = parseFloat(document.getElementById('transferAmount').value);

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
                showAlert('transferAlert', 'Transfer başarıyla gerçekleşti!', 'bg-green-100 text-green-700 dark:bg-green-900/50 dark:text-green-300');
                document.getElementById('transferForm').reset();
                await loadWalletData();
                await loadTransactions();
            } else if (response.status === 401 || response.status === 403) {
                handleUnauthorized();
            } else {
                const errText = await response.text();
                showAlert('transferAlert', `Hata: ${errText || 'Transfer başarısız.'}`, 'bg-red-100 text-red-700 dark:bg-red-900/50 dark:text-red-300');
            }
        } catch (error) {
            console.error('Transfer error:', error);
            showAlert('transferAlert', 'İşlem sırasında bir hata oluştu.', 'bg-red-100 text-red-700 dark:bg-red-900/50 dark:text-red-300');
        }
    });

    // --- Bakiye Yükleme Formu ---
    document.getElementById('depositForm')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const token = localStorage.getItem('jwtToken');
        if (!token) return handleUnauthorized();

        const toIban = document.getElementById('depositIban').value.trim();
        const amount = parseFloat(document.getElementById('depositAmount').value);

        try {
            const response = await fetch(`${API_BASE_URL}/wallets/deposit`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({ toIban, amount })
            });

            if (response.ok) {
                showAlert('depositAlert', 'Bakiye yükleme başarılı!', 'bg-green-100 text-green-700 dark:bg-green-900/50 dark:text-green-300');
                document.getElementById('depositForm').reset();
                setTimeout(() => closeModal('depositModal'), 1200);
                await loadWalletData();
                await loadTransactions();
            } else if (response.status === 401 || response.status === 403) {
                handleUnauthorized();
            } else {
                let errorMessage = 'Bakiye yükleme başarısız.';
                try {
                    const errData = await response.json();
                    errorMessage = errData.message || errorMessage;
                } catch (err) {
                    const errText = await response.text();
                    if (errText) errorMessage = errText;
                }
                showAlert('depositAlert', `Hata: ${errorMessage}`, 'bg-red-100 text-red-700 dark:bg-red-900/50 dark:text-red-300');
            }
        } catch (error) {
            console.error('Deposit error:', error);
            showAlert('depositAlert', 'Bakiye yüklenirken hata oluştu.', 'bg-red-100 text-red-700 dark:bg-red-900/50 dark:text-red-300');
        }
    });

    // --- Yeni Cüzdan Oluşturma Formu ---
    document.getElementById('createWalletForm')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const token = localStorage.getItem('jwtToken');
        if (!token) return handleUnauthorized();

        const curr = document.getElementById('newWalletCurrency').value;

        try {
            const response = await fetch(`${API_BASE_URL}/wallets/create?currency=${curr.toUpperCase()}`, {
                method: 'POST',
                headers: { 'Authorization': `Bearer ${token}` }
            });

            if (response.ok) {
                alert("Yeni cüzdanınız başarıyla oluşturuldu!");
                closeModal('createWalletModal');
                await loadWalletData();
            } else {
                let errMessage = 'Cüzdan oluşturulamadı.';
                try {
                    const err = await response.json();
                    errMessage = err.message || errMessage;
                } catch (e) {}
                alert(errMessage);
            }
        } catch (error) {
            console.error('Create wallet error:', error);
        }
    });

    // --- Döviz Dönüşüm Formu ---
    document.getElementById('exchangeForm')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const token = localStorage.getItem('jwtToken');
        if (!token) return handleUnauthorized();

        const fromIban = document.getElementById('exchangeFrom').value;
        const toIban = document.getElementById('exchangeTo').value;
        const amount = parseFloat(document.getElementById('exchangeAmount').value);

        if (fromIban === toIban) {
            alert("Aynı cüzdanlar arasında döviz dönüştürme yapamazsınız!");
            return;
        }

        try {
            const response = await fetch(`${API_BASE_URL}/wallets/exchange`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({ fromIban, toIban, amount })
            });

            if (response.ok) {
                alert("Döviz dönüşümü başarıyla tamamlandı!");
                closeModal('exchangeModal');
                await loadWalletData();
                await loadTransactions();
            } else {
                let errMsg = 'Dönüşüm başarısız.';
                try {
                    const err = await response.json();
                    errMsg = err.message || errMsg;
                } catch (e) {
                    errMsg = await response.text();
                }
                alert(errMsg);
            }
        } catch (error) {
            console.error('Exchange error:', error);
        }
    });

    // --- Ayarlar Modalı Açma ---
    document.getElementById('settingsBtn')?.addEventListener('click', async (e) => {
        e.preventDefault();
        await fetchUserProfile();
        openModal('settingsModal');
    });

    document.getElementById('closeSettingsBtn')?.addEventListener('click', () => {
        closeModal('settingsModal');
    });

    // --- Modal Dışına Tıklanınca Kapatma ---
    window.addEventListener('click', (e) => {
        if (e.target.id === 'settingsModal') closeModal('settingsModal');
        if (e.target.id === 'depositModal') closeModal('depositModal');
        if (e.target.id === 'exchangeModal') closeModal('exchangeModal');
        if (e.target.id === 'createWalletModal') closeModal('createWalletModal');
    });

    // --- Profil Güncelleme ---
    document.getElementById('updateProfileForm')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const token = localStorage.getItem('jwtToken');
        if (!token) return handleUnauthorized();

        const firstName = document.getElementById('profFirstName').value.trim();
        const lastName = document.getElementById('profLastName').value.trim();

        try {
            const response = await fetch(`${API_BASE_URL}/users/me/profile`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({ firstName, lastName })
            });

            if (response.ok) {
                showAlert('settingsAlert', 'Profil bilgileriniz başarıyla güncellendi!', 'bg-green-100 text-green-700 dark:bg-green-900/50 dark:text-green-300');
                document.getElementById('headerUserName').innerText = `${firstName} ${lastName}`;
            } else if (response.status === 401 || response.status === 403) {
                handleUnauthorized();
            } else {
                showAlert('settingsAlert', 'Profil güncellenirken bir hata oluştu.', 'bg-red-100 text-red-700 dark:bg-red-900/50 dark:text-red-300');
            }
        } catch (error) {
            console.error('Update profile error:', error);
            showAlert('settingsAlert', 'Sunucu hatası!', 'bg-red-100 text-red-700 dark:bg-red-900/50 dark:text-red-300');
        }
    });

    // --- E-Posta Güncelleme ---
    document.getElementById('updateEmailForm')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const token = localStorage.getItem('jwtToken');
        if (!token) return handleUnauthorized();

        const newEmail = document.getElementById('profEmail')?.value.trim();
        const currentPassword = document.getElementById('currentPasswordForEmail')?.value;

        if (!confirm("E-posta adresinizi değiştirmek üzeresiniz. Onaylıyor musunuz?")) return;

        try {
            const response = await fetch(`${API_BASE_URL}/users/me/email`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({ newEmail, currentPassword })
            });

            if (response.ok) {
                const data = await response.json();
                localStorage.setItem('jwtToken', data.token);
                showAlert('settingsAlert', 'E-posta başarıyla güncellendi! Sayfa yenileniyor...', 'bg-green-100 text-green-700 dark:bg-green-900/50 dark:text-green-300');
                setTimeout(() => location.reload(), 2000);
            } else if (response.status === 401 || response.status === 403) {
                handleUnauthorized();
            } else {
                let errorMsg = 'E-posta güncellenemedi.';
                try {
                    const errData = await response.json();
                    errorMsg = errData.message || errorMsg;
                } catch (err) {
                    const errText = await response.text();
                    if (errText) errorMsg = errText;
                }
                showAlert('settingsAlert', `Hata: ${errorMsg}`, 'bg-red-100 text-red-700 dark:bg-red-900/50 dark:text-red-300');
            }
        } catch (error) {
            console.error('Update email error:', error);
            showAlert('settingsAlert', 'Sunucuya bağlanılamadı!', 'bg-red-100 text-red-700 dark:bg-red-900/50 dark:text-red-300');
        }
    });

    // --- Şifre Güncelleme ---
    document.getElementById('updatePasswordForm')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const token = localStorage.getItem('jwtToken');
        if (!token) return handleUnauthorized();

        const oldPassword = document.getElementById('oldPassword').value;
        const newPassword = document.getElementById('newPassword').value;

        try {
            const response = await fetch(`${API_BASE_URL}/users/update-password`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({ oldPassword, newPassword })
            });

            if (response.ok) {
                showAlert('settingsAlert', 'Şifreniz başarıyla güncellendi!', 'bg-green-100 text-green-700 dark:bg-green-900/50 dark:text-green-300');
                document.getElementById('updatePasswordForm').reset();
                setTimeout(() => closeModal('settingsModal'), 2000);
            } else if (response.status === 401 || response.status === 403) {
                handleUnauthorized();
            } else {
                const errText = await response.text();
                let msg = "Şifre güncellenemedi.";
                try {
                    const errObj = JSON.parse(errText);
                    msg = errObj.message || msg;
                } catch (err) {
                    if (errText) msg = errText;
                }
                showAlert('settingsAlert', msg, 'bg-red-100 text-red-700 dark:bg-red-900/50 dark:text-red-300');
            }
        } catch (error) {
            console.error('Update password error:', error);
            showAlert('settingsAlert', 'Sunucu hatası!', 'bg-red-100 text-red-700 dark:bg-red-900/50 dark:text-red-300');
        }
    });
});

// --- Yardımcı ve Veri Yükleme Fonksiyonları ---

function updateDarkModeUI() {
    const isDark = document.documentElement.classList.contains('dark');
    const toggleBtn = document.getElementById('darkModeToggle');
    if (!toggleBtn) return;

    const dot = toggleBtn.querySelector('.dot');
    if (isDark) {
        dot?.classList.add('translate-x-6');
        toggleBtn.classList.add('bg-indigo-600');
        toggleBtn.classList.remove('bg-gray-300');
    } else {
        dot?.classList.remove('translate-x-6');
        toggleBtn.classList.add('bg-gray-300');
        toggleBtn.classList.remove('bg-indigo-600');
    }
}

async function showDashboard() {
    document.getElementById('authSection')?.classList.add('hidden');
    document.getElementById('dashboardSection')?.classList.remove('hidden');

    await fetchUserProfile();
    await loadWalletData();
    await loadTransactions();
}

async function fetchUserProfile() {
    const token = localStorage.getItem('jwtToken');
    if (!token) return;

    try {
        const response = await fetch(`${API_BASE_URL}/users/me/profile`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            const profile = await response.json();
            document.getElementById('headerUserName').innerText = `${profile.firstName || ''} ${profile.lastName || ''}`;
            document.getElementById('profFirstName').value = profile.firstName || '';
            document.getElementById('profLastName').value = profile.lastName || '';

            const emailField = document.getElementById('profEmail');
            if (emailField) emailField.value = profile.email || '';

            document.getElementById('statJoinDate').innerText = profile.createdAt ? new Date(profile.createdAt).toLocaleDateString('tr-TR') : "-";
            document.getElementById('statWalletCount').innerText = profile.totalWallets || "1";
        } else if (response.status === 401 || response.status === 403) {
            handleUnauthorized();
        }
    } catch (e) {
        console.error('Fetch profile error:', e);
    }
}

async function loadWalletData() {
    const token = localStorage.getItem('jwtToken');
    if (!token) return;

    try {
        const response = await fetch(`${API_BASE_URL}/wallets/me`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.ok) {
            const data = await response.json();
            // Backend'den dizi dönmüyorsa diziye çevir
            allWallets = Array.isArray(data) ? data : [data];

            updateWalletSelector();

            // Ekran yüklendiğinde var olan seçili cüzdanı veya ilk cüzdanı göster
            const currentSelectorVal = document.getElementById('walletSelector')?.value;
            const activeWallet = allWallets.find(w => w.iban === currentSelectorVal) || allWallets[0];

            renderActiveWallet(activeWallet);
        } else if (response.status === 401 || response.status === 403) {
            handleUnauthorized();
        }
    } catch (e) {
        console.error('Wallet load error:', e);
    }
}

async function loadTransactions() {
    const token = localStorage.getItem('jwtToken');
    if (!token) return;

    try {
        const response = await fetch(`${API_BASE_URL}/transactions/my?page=0&size=10`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.ok) {
            const data = await response.json();
            const tbody = document.getElementById('transactionTableBody');
            if (!tbody) return;

            tbody.innerHTML = '';
            const transactions = data.content || [];

            if (transactions.length === 0) {
                tbody.innerHTML = '<tr><td colspan="5" class="p-4 text-center text-gray-500 dark:text-gray-400">Henüz bir işlem bulunmuyor.</td></tr>';
                return;
            }

            const currentWalletIban = document.getElementById('walletIban')?.innerText || '';

            transactions.forEach(tx => {
                const isIncoming = currentWalletIban && tx.toIban === currentWalletIban;
                const currency = tx.currency || 'TL';

                const tr = document.createElement('tr');
                tr.className = "border-b dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-700/50 transition";
                tr.innerHTML = `
                    <td class="p-3">${new Date(tx.createdAt).toLocaleString('tr-TR')}</td>
                    <td class="p-3 font-mono text-xs">${tx.fromIban}</td>
                    <td class="p-3 font-mono text-xs">${tx.toIban}</td>
                    <td class="p-3 font-bold ${isIncoming ? 'text-green-600 dark:text-green-400' : 'text-red-600 dark:text-red-400'}">
                        ${isIncoming ? '+' : '-'}${tx.amount} ${currency}
                    </td>
                    <td class="p-3">
                        <button onclick="downloadPdf(${tx.id})" class="text-indigo-600 dark:text-indigo-400 hover:underline text-xs">
                             <i class="fa-solid fa-file-pdf"></i> Dekont
                        </button>
                    </td>
                `;
                tbody.appendChild(tr);
            });
        } else if (response.status === 401 || response.status === 403) {
            handleUnauthorized();
        }
    } catch (e) {
        console.error('Transactions load error:', e);
    }
}

async function downloadPdf(id) {
    const token = localStorage.getItem('jwtToken');
    if (!token) return;

    try {
        const response = await fetch(`${API_BASE_URL}/transactions/${id}/pdf`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.ok) {
            const blob = await response.blob();
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `dekont_${id}.pdf`;
            a.click();
            window.URL.revokeObjectURL(url);
        } else if (response.status === 401 || response.status === 403) {
            handleUnauthorized();
        }
    } catch (e) {
        console.error('PDF download error:', e);
    }
}

function handleUnauthorized() {
    localStorage.removeItem('jwtToken');
    window.location.reload();
}

function showAlert(elementId, message, bgClass) {
    const el = document.getElementById(elementId);
    if (!el) return;
    el.className = `p-3 text-sm rounded-lg ${bgClass}`;
    el.innerText = message;
    el.classList.remove('hidden');
    setTimeout(() => el.classList.add('hidden'), 5000);
}