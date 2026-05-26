const MemberModule = {
    page: 1,
    size: 10,
    sort: 'member_id',
    order: 'asc',
    keyword: '',

    init() {
        this.searchInput = document.getElementById('member-search');
        this.tableBody = document.querySelector('#member-table tbody');
        this.pageInfo = document.querySelector('#tab-members .page-info');
        this.prevBtn = document.querySelector('#tab-members .page-prev');
        this.nextBtn = document.querySelector('#tab-members .page-next');
        this.addBtn = document.getElementById('member-add-btn');

        this.searchInput.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') { this.keyword = e.target.value; this.page = 1; this.load(); }
        });

        document.querySelectorAll('#member-table th[data-sort]').forEach(th => {
            th.addEventListener('click', () => {
                const col = th.dataset.sort;
                if (this.sort === col) {
                    this.order = this.order === 'asc' ? 'desc' : 'asc';
                } else {
                    this.sort = col;
                    this.order = 'asc';
                }
                this.load();
            });
        });

        this.prevBtn.addEventListener('click', () => { if (this.page > 1) { this.page--; this.load(); } });
        this.nextBtn.addEventListener('click', () => { this.page++; this.load(); });
        this.addBtn.addEventListener('click', () => this.showForm(null));
    },

    async load() {
        try {
            const data = await API.get('/api/members', {
                keyword: this.keyword, sort: this.sort, order: this.order,
                page: this.page, size: this.size
            });
            this.render(data.list, data.total);
        } catch (e) { /* toast already shown */ }
    },

    render(list, total) {
        this.tableBody.innerHTML = '';
        if (!list || list.length === 0) {
            this.tableBody.innerHTML = '<tr><td colspan="6" class="empty-row">No data</td></tr>';
        } else {
            list.forEach(m => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>${escHtml(m.name)}</td>
                    <td>${escHtml(m.phone || '-')}</td>
                    <td>${escHtml(m.vipLevel)}</td>
                    <td>${m.tokenBalance}</td>
                    <td>￥${(m.accumulatedSpend || 0).toFixed(2)}</td>
                    <td>
                        <button class="btn-edit" data-id="${m.memberId}">Edit</button>
                        <button class="btn-del" data-id="${m.memberId}">Delete</button>
                    </td>`;
                tr.querySelector('.btn-edit').addEventListener('click', () => this.showForm(m));
                tr.querySelector('.btn-del').addEventListener('click', () => this.confirmDelete(m.memberId));
                this.tableBody.appendChild(tr);
            });
        }

        this.pageInfo.textContent = `Total ${total}, Page ${this.page}`;
        this.prevBtn.disabled = this.page <= 1;
        this.nextBtn.disabled = this.page * this.size >= total;

        // update sort indicators
        document.querySelectorAll('#member-table th[data-sort]').forEach(th => {
            th.classList.remove('asc', 'desc');
            if (th.dataset.sort === this.sort) {
                th.classList.add(this.order);
            }
        });
    },

    showForm(member) {
        const isEdit = !!member;
        document.getElementById('modal-title').textContent = isEdit ? 'Edit Member' : 'Add Member';
        document.getElementById('modal-body').innerHTML = `
            <div class="form-group">
                <label>Name <span style="color:red">*</span></label>
                <input id="form-name" value="${escHtml(member ? member.name : '')}">
            </div>
            <div class="form-group">
                <label>Phone</label>
                <input id="form-phone" value="${escHtml(member ? (member.phone || '') : '')}">
            </div>
            <div class="form-group">
                <label>VIP Level</label>
                <select id="form-vip">
                    <option value="Regular" ${member && member.vipLevel === 'Regular' ? 'selected' : ''}>Regular</option>
                    <option value="Silver" ${member && member.vipLevel === 'Silver' ? 'selected' : ''}>Silver</option>
                    <option value="Gold" ${member && member.vipLevel === 'Gold' ? 'selected' : ''}>Gold</option>
                </select>
            </div>
            ${isEdit ? `
            <div class="form-group">
                <label>Token Balance</label>
                <input id="form-balance" type="number" value="${member.tokenBalance || 0}">
            </div>` : ''}`;

        Modal.show(async () => {
            const name = document.getElementById('form-name').value.trim();
            const phone = document.getElementById('form-phone').value.trim();
            const vipLevel = document.getElementById('form-vip').value;

            const body = { name, phone, vipLevel };
            if (isEdit) {
                body.tokenBalance = parseInt(document.getElementById('form-balance').value) || 0;
                await API.put(`/api/members/${member.memberId}`, body);
            } else {
                await API.post('/api/members', body);
            }
            Modal.hide();
            this.load();
        });
    },

    async confirmDelete(id) {
        if (!confirm('Delete this member?')) return;
        try {
            await API.del(`/api/members/${id}`);
            this.load();
        } catch (e) { /* toast already shown */ }
    }
};

function escHtml(str) {
    if (!str) return '';
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
}
