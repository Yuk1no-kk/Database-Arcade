const TransactionModule = {
    page: 1,
    size: 10,

    init() {
        this.tableBody = document.querySelector('#transaction-table tbody');
        this.pageInfo = document.querySelector('#tab-transactions .page-info');
        this.prevBtn = document.querySelector('#tab-transactions .page-prev');
        this.nextBtn = document.querySelector('#tab-transactions .page-next');

        document.getElementById('recharge-btn').addEventListener('click', () => this.showRechargeForm());
        document.getElementById('consume-btn').addEventListener('click', () => this.showConsumeForm());

        this.prevBtn.addEventListener('click', () => { if (this.page > 1) { this.page--; this.load(); } });
        this.nextBtn.addEventListener('click', () => { this.page++; this.load(); });
    },

    async load() {
        try {
            const data = await API.get('/api/transactions', { page: this.page, size: this.size });
            this.render(data.list, data.total);
        } catch (e) { /* toast already shown */ }
    },

    render(list, total) {
        this.tableBody.innerHTML = '';
        if (!list || list.length === 0) {
            this.tableBody.innerHTML = '<tr><td colspan="5" class="empty-row">暂无交易记录</td></tr>';
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

        this.pageInfo.textContent = `共 ${total} 条，第 ${this.page} 页`;
        this.prevBtn.disabled = this.page <= 1;
        this.nextBtn.disabled = this.page * this.size >= total;
    },

    async showRechargeForm() {
        let members;
        try {
            members = await API.get('/api/members', { size: 999 });
        } catch (e) { return; }

        if (!members.list || members.list.length === 0) {
            alert('暂无会员，请先新增会员');
            return;
        }

        const memberOpts = members.list.map(m => `<option value="${m.memberId}">${escHtml(m.name)} (余额:${m.tokenBalance})</option>`).join('');

        document.getElementById('modal-title').textContent = '充值';
        document.getElementById('modal-body').innerHTML = `
            <div class="form-group">
                <label>选择会员</label>
                <select id="form-member">${memberOpts}</select>
            </div>
            <div class="form-group">
                <label>充值金额 (￥) <span style="color:#888; font-size:12px">1元 = 10代币</span></label>
                <input id="form-amount" type="number" min="1" step="0.01" placeholder="输入金额">
            </div>`;

        Modal.show(async () => {
            const memberId = parseInt(document.getElementById('form-member').value);
            const amount = parseFloat(document.getElementById('form-amount').value);
            if (!amount || amount <= 0) {
                alert('请输入有效金额');
                throw new Error('invalid amount');
            }
            await API.post('/api/transactions/recharge', { memberId, amount });
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
            alert('暂无会员，请先新增会员');
            return;
        }
        if (!machines.list || machines.list.length === 0) {
            alert('暂无可用游戏机，请先新增游戏机');
            return;
        }

        const memberOpts = members.list.map(m => `<option value="${m.memberId}">${escHtml(m.name)} (余额:${m.tokenBalance})</option>`).join('');
        const machineOpts = machines.list.map(m => `<option value="${m.machineId}">${escHtml(m.name)} (${m.tokensPerGame}币/次)</option>`).join('');

        document.getElementById('modal-title').textContent = '消费扣币';
        document.getElementById('modal-body').innerHTML = `
            <div class="form-group">
                <label>选择会员</label>
                <select id="form-member">${memberOpts}</select>
            </div>
            <div class="form-group">
                <label>选择游戏机</label>
                <select id="form-machine">${machineOpts}</select>
            </div>
            <div class="form-group">
                <label>消耗代币数</label>
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
