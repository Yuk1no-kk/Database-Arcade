// Tab switching
document.querySelectorAll('.tab-btn').forEach(btn => {
    btn.addEventListener('click', () => {
        document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
        document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));
        btn.classList.add('active');
        document.getElementById('tab-' + btn.dataset.tab).classList.add('active');

        // reload current tab data
        if (btn.dataset.tab === 'members') MemberModule.load();
        else if (btn.dataset.tab === 'machines') MachineModule.load();
        else if (btn.dataset.tab === 'transactions') TransactionModule.load();
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
            // error toast already shown in api.js
            document.getElementById('modal-submit').disabled = false;
        }
        document.getElementById('modal-submit').disabled = false;
    }
});

// Init modules
MemberModule.init();
MachineModule.init();
TransactionModule.init();

// Load first tab on page load
MemberModule.load();
