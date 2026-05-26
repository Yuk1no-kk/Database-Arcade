const StaffModule = {
    page: 1,
    size: 10,
    sort: 'staff_id',
    order: 'asc',
    keyword: '',
    selfId: null,

    init(selfId) {
        this.selfId = selfId;
        this.searchInput = document.getElementById('staff-search');
        this.tableBody = document.querySelector('#staff-table tbody');
        this.pageInfo = document.querySelector('#tab-staff .page-info');
        this.prevBtn = document.querySelector('#tab-staff .page-prev');
        this.nextBtn = document.querySelector('#tab-staff .page-next');

        this.searchInput.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') { this.keyword = e.target.value; this.page = 1; this.load(); }
        });

        document.querySelectorAll('#staff-table th[data-sort]').forEach(th => {
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
    },

    async load() {
        try {
            const data = await API.get('/api/staff', {
                excludeId: this.selfId,
                keyword: this.keyword, sort: this.sort, order: this.order,
                page: this.page, size: this.size
            });
            this.render(data.list, data.total);
        } catch (e) { /* toast already shown */ }
    },

    render(list, total) {
        this.tableBody.innerHTML = '';
        if (!list || list.length === 0) {
            this.tableBody.innerHTML = '<tr><td colspan="3" class="empty-row">No staff</td></tr>';
        } else {
            list.forEach(s => {
                const roleText = s.permissionLevel === 'admin' ? 'Admin' : 'Worker';
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>${escHtml(s.username)}</td>
                    <td>${roleText}</td>
                    <td>
                        <button class="btn-edit" data-id="${s.staffId}" data-role="${s.permissionLevel}">Change Role</button>
                        <button class="btn-del" data-id="${s.staffId}">Delete</button>
                    </td>`;
                tr.querySelector('.btn-edit').addEventListener('click', () => this.showChangeRole(s.staffId, s.permissionLevel));
                tr.querySelector('.btn-del').addEventListener('click', () => this.confirmDelete(s.staffId));
                this.tableBody.appendChild(tr);
            });
        }

        this.pageInfo.textContent = `Total ${total}, Page ${this.page}`;
        this.prevBtn.disabled = this.page <= 1;
        this.nextBtn.disabled = this.page * this.size >= total;

        document.querySelectorAll('#staff-table th[data-sort]').forEach(th => {
            th.classList.remove('asc', 'desc');
            if (th.dataset.sort === this.sort) {
                th.classList.add(this.order);
            }
        });
    },

    showChangeRole(staffId, currentRole) {
        document.getElementById('modal-title').textContent = 'Change Role';
        document.getElementById('modal-body').innerHTML = `
            <div class="form-group">
                <label>Role</label>
                <select id="form-role">
                    <option value="worker" ${currentRole === 'worker' ? 'selected' : ''}>Worker</option>
                    <option value="admin" ${currentRole === 'admin' ? 'selected' : ''}>Admin</option>
                </select>
            </div>`;

        Modal.show(async () => {
            const permissionLevel = document.getElementById('form-role').value;
            await API.put(`/api/staff/${staffId}/permission`, { permissionLevel });
            Modal.hide();
            this.load();
        });
    },

    async confirmDelete(id) {
        if (!confirm('Delete this staff member?')) return;
        try {
            await API.del(`/api/staff/${id}`);
            this.load();
        } catch (e) { /* toast already shown */ }
    }
};
