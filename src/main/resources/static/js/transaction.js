const TransactionModule = {
    page: 1,
    size: 10,
    keyword: '',

    init() {
        this.searchInput = document.getElementById('transaction-search');
        this.tableBody = document.querySelector('#transaction-table tbody');
        this.pageInfo = document.querySelector('#tab-transactions .page-info');
        this.prevBtn = document.querySelector('#tab-transactions .page-prev');
        this.nextBtn = document.querySelector('#tab-transactions .page-next');

        document.getElementById('recharge-btn').addEventListener('click', () => this.showRechargeForm());
        document.getElementById('consume-btn').addEventListener('click', () => this.showConsumeForm());
        document.getElementById('package-btn').addEventListener('click', () => this.showPackageManager());

        this.searchInput.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') { this.keyword = e.target.value; this.page = 1; this.load(); }
        });

        this.prevBtn.addEventListener('click', () => { if (this.page > 1) { this.page--; this.load(); } });
        this.nextBtn.addEventListener('click', () => { this.page++; this.load(); });
    },

    async load() {
        try {
            const params = { page: this.page, size: this.size };
            if (this.keyword) params.keyword = this.keyword;
            const data = await API.get('/api/transactions', params);
            this.render(data.list, data.total);
        } catch (e) { /* toast already shown */ }
    },

    render(list, total) {
        this.tableBody.innerHTML = '';
        if (!list || list.length === 0) {
            this.tableBody.innerHTML = '<tr><td colspan="5" class="empty-row">No transactions</td></tr>';
        } else {
            list.forEach(row => {
                const tr = document.createElement('tr');
                const time = row.time ? new Date(row.time).toLocaleString('zh-CN') : '-';
                const isRecharge = row.type === 'recharge';
                const tokensDisplay = isRecharge ? `+${row.tokens}` : `${row.tokens}`;
                const amountDisplay = row.amount != null ? `￥${parseFloat(row.amount).toFixed(2)}` : '-';
                const machineDisplay = row.machine_name || '-';

                tr.innerHTML = `
                    <td>${escHtml(row.member_name)}</td>
                    <td>${amountDisplay}</td>
                    <td>${tokensDisplay}</td>
                    <td>${machineDisplay}</td>
                    <td>${time}</td>`;
                this.tableBody.appendChild(tr);
            });
        }

        this.pageInfo.textContent = `Total ${total}, Page ${this.page}`;
        this.prevBtn.disabled = this.page <= 1;
        this.nextBtn.disabled = this.page * this.size >= total;
    },

    // ---- Package Management ----

    async showPackageManager() {
        let packages;
        try {
            packages = await API.get('/api/packages');
        } catch (e) { return; }

        document.getElementById('modal-submit').textContent = 'Add Package';

        let html = '';
        if (!packages || packages.length === 0) {
            html = '<p style="text-align:center;color:#aaa;padding:20px;">No packages</p>';
        } else {
            html = '<table style="width:100%;border-collapse:collapse;">';
            html += '<thead><tr><th style="text-align:left;padding:8px;">Name</th><th style="text-align:left;padding:8px;">Price</th><th style="text-align:left;padding:8px;">Tokens</th><th style="text-align:left;padding:8px;">Actions</th></tr></thead><tbody>';
            packages.forEach(p => {
                html += `<tr>
                    <td style="padding:8px;">${escHtml(p.packageName)}</td>
                    <td style="padding:8px;">￥${parseFloat(p.price).toFixed(2)}</td>
                    <td style="padding:8px;">${p.tokenCount}</td>
                    <td style="padding:8px;">
                        <button class="btn-edit" data-pkg='${JSON.stringify(p).replace(/'/g, "&#39;")}'>Edit</button>
                        <button class="btn-del" data-id="${p.packageId}">Delete</button>
                    </td></tr>`;
            });
            html += '</tbody></table>';
        }

        document.getElementById('modal-title').textContent = 'Package Management';
        document.getElementById('modal-body').innerHTML = html;

        // Bind edit buttons
        document.querySelectorAll('#modal-body .btn-edit').forEach(btn => {
            btn.addEventListener('click', () => {
                const pkg = JSON.parse(btn.dataset.pkg);
                this.showPackageForm(pkg);
            });
        });

        // Bind delete buttons
        document.querySelectorAll('#modal-body .btn-del').forEach(btn => {
            btn.addEventListener('click', async () => {
                if (!confirm('Delete this package?')) return;
                try {
                    await API.del(`/api/packages/${btn.dataset.id}`);
                    Modal.hide();
                    this.showPackageManager();
                } catch (e) { /* toast already shown */ }
            });
        });

        Modal.show(async () => {
            Modal.hide();
            this.showPackageForm(null);
        });

        document.getElementById('modal-submit').textContent = 'Add Package';
    },

    showPackageForm(pkg) {
        const isEdit = !!pkg;
        document.getElementById('modal-title').textContent = isEdit ? 'Edit Package' : 'Add Package';
        document.getElementById('modal-submit').textContent = isEdit ? 'OK' : 'Add Package';
        document.getElementById('modal-body').innerHTML = `
            <div class="form-group">
                <label>Package Name <span style="color:red">*</span></label>
                <input id="form-pkg-name" value="${escHtml(isEdit ? pkg.packageName : '')}" placeholder="e.g. $10 Pack">
            </div>
            <div class="form-group">
                <label>Price (￥) <span style="color:red">*</span></label>
                <input id="form-pkg-price" type="number" min="0.01" step="0.01" value="${isEdit ? pkg.price : ''}">
            </div>
            <div class="form-group">
                <label>Token Count <span style="color:red">*</span></label>
                <input id="form-pkg-tokens" type="number" min="1" value="${isEdit ? pkg.tokenCount : ''}">
            </div>`;

        Modal.show(async () => {
            const packageName = document.getElementById('form-pkg-name').value.trim();
            const price = parseFloat(document.getElementById('form-pkg-price').value);
            const tokenCount = parseInt(document.getElementById('form-pkg-tokens').value);

            const body = { packageName, price, tokenCount };
            if (isEdit) {
                await API.put(`/api/packages/${pkg.packageId}`, body);
            } else {
                await API.post('/api/packages', body);
            }
            Modal.hide();
            this.showPackageManager();
        });
    },

    // ---- Recharge (dual-mode) ----

    async showRechargeForm() {
        let members, packages;
        try {
            members = await API.get('/api/members', { size: 999 });
            packages = await API.get('/api/packages');
        } catch (e) { return; }

        if (!members.list || members.list.length === 0) {
            alert('No members. Please add a member first.');
            return;
        }

        const memberOpts = members.list.map(m => `<option value="${m.memberId}">${escHtml(m.name)} (Balance:${m.tokenBalance})</option>`).join('');

        let packageOpts = '';
        if (packages && packages.length > 0) {
            packageOpts = packages.map(p => `<option value="${p.packageId}">${escHtml(p.packageName)} — ￥${parseFloat(p.price).toFixed(2)} / ${p.tokenCount} Tokens</option>`).join('');
        }

        document.getElementById('modal-title').textContent = 'Recharge';
        document.getElementById('modal-submit').textContent = 'OK';
        document.getElementById('modal-body').innerHTML = `
            <div class="form-group">
                <label>Select Member</label>
                <select id="form-member">${memberOpts}</select>
            </div>
            <div class="form-group">
                <label>Recharge Mode</label>
                <select id="form-recharge-mode">
                    <option value="direct">Direct (1 RMB = 10 Tokens)</option>
                    <option value="package" ${!packageOpts ? 'disabled' : ''}>Package</option>
                </select>
            </div>
            <div id="recharge-direct">
                <div class="form-group">
                    <label>Amount (￥)</label>
                    <input id="form-amount" type="number" min="1" step="0.01" placeholder="Enter amount">
                </div>
            </div>
            <div id="recharge-package" style="display:none;">
                <div class="form-group">
                    <label>Select Package</label>
                    <select id="form-package">${packageOpts || '<option disabled>No packages available</option>'}</select>
                </div>
            </div>`;

        // Toggle direct / package
        const modeSelect = document.getElementById('form-recharge-mode');
        const directDiv = document.getElementById('recharge-direct');
        const packageDiv = document.getElementById('recharge-package');
        modeSelect.addEventListener('change', () => {
            if (modeSelect.value === 'direct') {
                directDiv.style.display = '';
                packageDiv.style.display = 'none';
            } else {
                directDiv.style.display = 'none';
                packageDiv.style.display = '';
            }
        });

        Modal.show(async () => {
            const memberId = parseInt(document.getElementById('form-member').value);
            const mode = document.getElementById('form-recharge-mode').value;

            if (mode === 'direct') {
                const amount = parseFloat(document.getElementById('form-amount').value);
                if (!amount || amount <= 0) {
                    alert('Please enter a valid amount');
                    throw new Error('invalid amount');
                }
                await API.post('/api/transactions/recharge', { memberId, amount });
            } else {
                const packageId = parseInt(document.getElementById('form-package').value);
                if (!packageId) {
                    alert('Please select a package');
                    throw new Error('no package selected');
                }
                await API.post('/api/transactions/recharge', { memberId, packageId });
            }

            Modal.hide();
            this.load();
            if (typeof MemberModule !== 'undefined') MemberModule.load();
        });
    },

    async showConsumeForm() {
        let members, machines;
        try {
            members = await API.get('/api/members', { size: 999 });
            machines = await API.get('/api/machines', { size: 999 });
        } catch (e) { return; }

        if (!members.list || members.list.length === 0) {
            alert('No members. Please add a member first.');
            return;
        }
        if (!machines.list || machines.list.length === 0) {
            alert('No machines. Please add a machine first.');
            return;
        }

        const memberOpts = members.list.map(m => `<option value="${m.memberId}">${escHtml(m.name)} (Balance:${m.tokenBalance})</option>`).join('');
        const machineOpts = machines.list.map(m => `<option value="${m.machineId}">${escHtml(m.name)} (${m.tokensPerGame} tokens/game)</option>`).join('');

        document.getElementById('modal-title').textContent = 'Consume Tokens';
        document.getElementById('modal-submit').textContent = 'OK';
        document.getElementById('modal-body').innerHTML = `
            <div class="form-group">
                <label>Select Member</label>
                <select id="form-member">${memberOpts}</select>
            </div>
            <div class="form-group">
                <label>Select Machine</label>
                <select id="form-machine">${machineOpts}</select>
            </div>
            <div class="form-group">
                <label>Token Count</label>
                <input id="form-tokens" type="number" value="1" min="1">
            </div>`;

        Modal.show(async () => {
            const memberId = parseInt(document.getElementById('form-member').value);
            const machineId = parseInt(document.getElementById('form-machine').value);
            const tokens = parseInt(document.getElementById('form-tokens').value) || 1;
            await API.post('/api/transactions/consume', { memberId, machineId, tokens });
            Modal.hide();
            this.load();
            if (typeof MemberModule !== 'undefined') MemberModule.load();
        });
    }
};
