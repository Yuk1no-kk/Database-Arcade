// Login check
const user = JSON.parse(sessionStorage.getItem('user'));
if (!user) {
    window.location.href = '/login.html';
}

document.getElementById('current-user').textContent = `${user.name || user.username} (${user.permissionLevel === 'admin' ? 'Admin' : 'Worker'})`;

document.getElementById('logout-btn').addEventListener('click', () => {
    sessionStorage.clear();
    window.location.href = '/login.html';
});

// Show staff tab for admin
if (user.permissionLevel === 'admin') {
    document.querySelectorAll('.admin-only').forEach(el => el.classList.remove('hidden'));
}

// Tab switching
document.querySelectorAll('.tab-btn').forEach(btn => {
    btn.addEventListener('click', () => {
        document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
        document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));
        btn.classList.add('active');
        document.getElementById('tab-' + btn.dataset.tab).classList.add('active');

        if (btn.dataset.tab === 'members') MemberModule.load();
        else if (btn.dataset.tab === 'machines') MachineModule.load();
        else if (btn.dataset.tab === 'transactions') TransactionModule.load();
        else if (btn.dataset.tab === 'staff') StaffModule.load();
    });
});

// Modal logic
const Modal = {
    overlay: document.getElementById('modal-overlay'),
    submitHandler: null,

    show(handler) {
        this.submitHandler = handler;
        this.overlay.classList.remove('hidden');
        document.getElementById('modal-close').focus();
    },

    hide() {
        this.overlay.classList.add('hidden');
        this.submitHandler = null;
    }
};

document.getElementById('modal-close').addEventListener('click', () => Modal.hide());
document.getElementById('modal-cancel').addEventListener('click', () => Modal.hide());
document.getElementById('modal-overlay').addEventListener('click', (e) => {
    if (e.target === Modal.overlay) Modal.hide();
});
document.getElementById('modal-submit').addEventListener('click', async () => {
    if (Modal.submitHandler) {
        document.getElementById('modal-submit').disabled = true;
        try {
            await Modal.submitHandler();
        } catch (e) {
            document.getElementById('modal-submit').disabled = false;
        }
        document.getElementById('modal-submit').disabled = false;
    }
});

// Init modules
MemberModule.init();
MachineModule.init();
TransactionModule.init();
if (user.permissionLevel === 'admin') {
    StaffModule.init(user.staffId);
}

// Load first tab on page load
MemberModule.load();
