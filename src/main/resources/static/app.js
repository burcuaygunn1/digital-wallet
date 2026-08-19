const API_BASE_URL = 'http://localhost:8080/api/v1';
let allWallets = [];
function showToast(message, type = 'success') {
    const container = document.getElementById('toastContainer');
    if (!container) return;

    const toast = document.createElement('div');

    let bgColor = 'bg-white';
    let textColor = 'text-gray-800';
    let icon = 'fa-circle-info';
    let borderColor = 'border-blue-500';

    if (type === 'success') {
        borderColor = 'border-green-500';
        icon = 'fa-circle-check text-green-500';
    } else if (type === 'error') {
        borderColor = 'border-red-500';
        icon = 'fa-circle-xmark text-red-500';
    } else if (type === 'warning') {
        borderColor = 'border-yellow-500';
        icon = 'fa-triangle-exclamation text-yellow-500';
    } else if (type === 'info') {
        borderColor = 'border-blue-500';
        icon = 'fa-circle-info text-blue-500';
    }

    toast.className = `flex items-center p-4 min-w-[300px] ${bgColor} ${textColor} rounded-xl shadow-2xl border-l-4 ${borderColor} transform translate-x-full transition-all duration-300 ease-out opacity-0 dark:bg-gray-800 dark:text-white`;

    toast.innerHTML = `
        <div class="flex-shrink-0 text-xl mr-3">
            <i class="fa-solid ${icon}"></i>
        </div>
        <div class="flex-1 text-sm font-medium">
            ${message}
        </div>
        <button class="ml-4 text-gray-400 hover:text-gray-600 dark:hover:text-gray-200">
            <i class="fa-solid fa-xmark"></i>
        </button>
    `;

    container.appendChild(toast);

    setTimeout(() => {
        toast.classList.remove('translate-x-full', 'opacity-0');
    }, 10);

    const closeBtn = toast.querySelector('button');
    closeBtn.onclick = () => removeToast(toast);

    setTimeout(() => {
        removeToast(toast);
    }, 4000);
}

function removeToast(toast) {
    if (!toast) return;
    toast.classList.add('translate-x-full', 'opacity-0');
    setTimeout(() => {
        if (toast.parentNode) toast.parentNode.removeChild(toast);
    }, 300);
}

function showAlert(elementId, message, bgClass) {
    const isError = bgClass && (bgClass.includes('red') || bgClass.includes('error'));
    showToast(message, isError ? 'error' : 'success');
}
if (localStorage.getItem('theme') === 'dark') {
    document.documentElement.classList.add('dark');
}
function openModal(modalId) {
    document.getElementById(modalId)?.classList.remove('hidden');
}

function closeModal(modalId) {
    document.getElementById(modalId)?.classList.add('hidden');
    if (modalId === 'pdfModal') {
        const iframe = document.getElementById('pdfIframe');
        if (iframe && iframe.src) {
            window.URL.revokeObjectURL(iframe.src);
            iframe.src = '';
        }
    }
}

function openDepositModal() {
    const currentIban = document.getElementById('walletIban')?.innerText;
    if (currentIban) {
        document.getElementById('depositIban').value = currentIban;
    }
    openModal('depositModal');
}

function openExchangeModal() {
    updateWalletSelector();
    openModal('exchangeModal');
}

function openCreateWalletModal() {
    openModal('createWalletModal');
}
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
function updateWalletSelector() {
    const selector = document.getElementById('walletSelector');
    if (selector) {
        selector.innerHTML = allWallets.map(w => `<option value="${w.iban}">${w.currency} Cüzdanı</option>`).join('');
    }

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
        loadTransactions(); // Seçilen cüzdana göre işlemleri yenile
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
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            const targetTab = btn.getAttribute('data-tab');
            if (targetTab) switchTab(targetTab, btn);
        });
    });
    document.getElementById('darkModeToggle')?.addEventListener('click', () => {
        const isDark = document.documentElement.classList.toggle('dark');
        localStorage.setItem('theme', isDark ? 'dark' : 'light');
        updateDarkModeUI();
    });
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
                showToast('Kayıt başarıyla tamamlandı!', 'success');
                showDashboard();
            } else {
                let message = "Kayıt işlemi başarısız.";
                try {
                    const errorData = await response.json();
                    message = errorData.message || message;
                } catch (err) {
                    message = await response.text() || message;
                }
                showToast(message, 'error');
            }
        } catch (error) {
            console.error('Register error:', error);
            showToast('Sunucuya bağlanılamadı!', 'error');
        }
    });
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
                showToast('Giriş başarılı!', 'success');
                showDashboard();
            } else {
                let message = "Giriş başarısız! Bilgilerinizi kontrol edin.";
                try {
                    const errorData = await response.json();
                    message = errorData.message || message;
                } catch (err) {
                    message = await response.text() || message;
                }
                showToast(message, 'error');
            }
        } catch (error) {
            console.error('Login error:', error);
            showToast('Sunucuya bağlanılamadı!', 'error');
        }
    });
    document.getElementById('logoutBtn')?.addEventListener('click', () => {
        localStorage.removeItem('jwtToken');
        window.location.reload();
    });
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
                let successMsg = "Transfer başarıyla gerçekleşti!";
                try {
                    const text = await response.text();
                    if (text) successMsg = text;
                } catch (e) {}

                showToast(successMsg, 'success');
                document.getElementById('transferForm').reset();
                await loadWalletData();
                await loadTransactions();
            } else if (response.status === 401 || response.status === 403) {
                handleUnauthorized();
            } else {
                let message = "Transfer işlemi başarısız.";
                try {
                    const errorData = await response.json();
                    message = errorData.message || message;
                } catch (err) {
                    message = await response.text() || message;
                }
                showToast(message, 'error');
            }
        } catch (error) {
            console.error('Transfer error:', error);
            showToast('İşlem sırasında bir hata oluştu.', 'error');
        }
    });
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
                showToast("Bakiye başarıyla yüklendi!", 'success');
                document.getElementById('depositForm').reset();
                closeModal('depositModal');
                await loadWalletData();
                await loadTransactions();
            } else if (response.status === 401 || response.status === 403) {
                handleUnauthorized();
            } else {
                let message = "Bakiye yükleme başarısız.";
                try {
                    const errorData = await response.json();
                    message = errorData.message || message;
                } catch (err) {
                    message = await response.text() || message;
                }
                showToast(message, 'error');
            }
        } catch (error) {
            console.error('Deposit error:', error);
            showToast('Bakiye yüklenirken bir hata oluştu.', 'error');
        }
    });
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
                showToast("Yeni cüzdanınız başarıyla oluşturuldu!", 'success');
                closeModal('createWalletModal');
                await loadWalletData();
            } else {
                let message = "Cüzdan oluşturulamadı.";
                try {
                    const errorData = await response.json();
                    message = errorData.message || message;
                } catch (err) {
                    message = await response.text() || message;
                }
                showToast(message, 'error');
            }
        } catch (error) {
            console.error('Create wallet error:', error);
            showToast("Sunucu hatası oluştu.", 'error');
        }
    });
    document.getElementById('exchangeForm')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const token = localStorage.getItem('jwtToken');
        if (!token) return handleUnauthorized();

        const fromIban = document.getElementById('exchangeFrom').value;
        const toIban = document.getElementById('exchangeTo').value;
        const amount = parseFloat(document.getElementById('exchangeAmount').value);

        if (fromIban === toIban) {
            showToast("Aynı cüzdanlar arasında döviz dönüştürme yapamazsınız!", 'warning');
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
                showToast("Döviz dönüşümü başarıyla tamamlandı!", 'success');
                closeModal('exchangeModal');
                await loadWalletData();
                await loadTransactions();
            } else {
                let message = "Dönüşüm başarısız.";
                try {
                    const errorData = await response.json();
                    message = errorData.message || message;
                } catch (err) {
                    message = await response.text() || message;
                }
                showToast(message, 'error');
            }
        } catch (error) {
            console.error('Exchange error:', error);
            showToast("Dönüşüm gerçekleştirilirken bir hata oluştu.", 'error');
        }
    });
    document.getElementById('settingsBtn')?.addEventListener('click', async (e) => {
        e.preventDefault();
        await fetchUserProfile();
        openModal('settingsModal');
    });

    document.getElementById('closeSettingsBtn')?.addEventListener('click', () => {
        closeModal('settingsModal');
    });
    window.addEventListener('click', (e) => {
        if (e.target.id === 'settingsModal') closeModal('settingsModal');
        if (e.target.id === 'depositModal') closeModal('depositModal');
        if (e.target.id === 'exchangeModal') closeModal('exchangeModal');
        if (e.target.id === 'createWalletModal') closeModal('createWalletModal');
        if (e.target.id === 'pdfModal') closeModal('pdfModal');
    });
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
                showToast('Profil bilgileriniz başarıyla güncellendi!', 'success');
                document.getElementById('headerUserName').innerText = `${firstName} ${lastName}`;
            } else if (response.status === 401 || response.status === 403) {
                handleUnauthorized();
            } else {
                let message = "Profil güncellenirken bir hata oluştu.";
                try {
                    const errorData = await response.json();
                    message = errorData.message || message;
                } catch (err) {
                    message = await response.text() || message;
                }
                showToast(message, 'error');
            }
        } catch (error) {
            console.error('Update profile error:', error);
            showToast('Sunucu hatası!', 'error');
        }
    });
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
                showToast('E-posta adresiniz başarıyla güncellendi.', 'success');
                setTimeout(() => location.reload(), 2000);
            } else if (response.status === 401 || response.status === 403) {
                handleUnauthorized();
            } else {
                let message = "E-posta güncellenemedi.";
                try {
                    const errorData = await response.json();
                    message = errorData.message || message;
                } catch (err) {
                    message = await response.text() || message;
                }
                showToast(message, 'error');
            }
        } catch (error) {
            console.error('Update email error:', error);
            showToast('Sunucuya bağlanılamadı!', 'error');
        }
    });
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
                showToast('Şifreniz başarıyla güncellendi!', 'success');
                document.getElementById('updatePasswordForm').reset();
                setTimeout(() => closeModal('settingsModal'), 1500);
            } else if (response.status === 401 || response.status === 403) {
                handleUnauthorized();
            } else {
                let message = "Şifre güncellenemedi.";
                try {
                    const errorData = await response.json();
                    message = errorData.message || message;
                } catch (err) {
                    message = await response.text() || message;
                }
                showToast(message, 'error');
            }
        } catch (error) {
            console.error('Update password error:', error);
            showToast('Sunucu hatası!', 'error');
        }
    });
});

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
            allWallets = Array.isArray(data) ? data : [data];

            updateWalletSelector();

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

            const currentIban = document.getElementById('walletIban')?.innerText || '';

            transactions.forEach(tx => {
                const isIncoming = currentIban && tx.toIban === currentIban;
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
                    <td class="p-3 flex items-center justify-center space-x-3">
                        <!-- Görüntüle Butonu -->
                        <button onclick="handlePdf(${tx.id}, 'view')" class="text-blue-500 hover:text-blue-700 transition" title="Önizle">
                            <i class="fa-solid fa-eye text-base"></i>
                        </button>
                        <!-- İndir Butonu -->
                        <button onclick="handlePdf(${tx.id}, 'download')" class="text-indigo-600 hover:text-indigo-800 dark:text-indigo-400 transition" title="İndir">
                            <i class="fa-solid fa-file-arrow-down text-base"></i>
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
async function handlePdf(id, action) {
    const token = localStorage.getItem('jwtToken');
    if (!token) return handleUnauthorized();
    const actionParam = action === 'download' ? 'download' : 'inline';
    const url = `${API_BASE_URL}/transactions/${id}/pdf?action=${actionParam}&t=${Date.now()}`;

    try {
        const response = await fetch(url, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.ok) {
            const blob = await response.blob();
            const fileUrl = window.URL.createObjectURL(blob);

            if (action === 'download') {
                const a = document.createElement('a');
                a.href = fileUrl;
                a.download = `dekont_${id}.pdf`;
                document.body.appendChild(a);
                a.click();
                document.body.removeChild(a);
                window.URL.revokeObjectURL(fileUrl);
                showToast('Dekont indirildi.', 'info');
            } else {
                const iframe = document.getElementById('pdfIframe');
                const downloadLink = document.getElementById('pdfDownloadLink');

                if (iframe) iframe.src = fileUrl;
                if (downloadLink) {
                    downloadLink.href = fileUrl;
                    downloadLink.download = `dekont_${id}.pdf`;
                }

                openModal('pdfModal');
            }
        } else if (response.status === 401 || response.status === 403) {
            handleUnauthorized();
        } else {
            showToast('Dekont oluşturulamadı.', 'error');
        }
    } catch (e) {
        console.error('PDF error:', e);
        showToast('Dekont alınırken bir hata oluştu.', 'error');
    }
}

function handleUnauthorized() {
    localStorage.removeItem('jwtToken');
    window.location.reload();
}