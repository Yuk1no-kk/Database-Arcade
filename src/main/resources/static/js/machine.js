const MachineModule = {
    page: 1,
    size: 10,
    sort: 'machine_id',
    order: 'asc',
    keyword: '',

    init() {
        this.searchInput = document.getElementById('machine-search');
        this.tableBody = document.querySelector('#machine-table tbody');
        this.pageInfo = document.querySelector('#tab-machines .page-info');
        this.prevBtn = document.querySelector('#tab-machines .page-prev');
        this.nextBtn = document.querySelector('#tab-machines .page-next');
        this.addBtn = document.getElementById('machine-add-btn');

        this.searchInput.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') { this.keyword = e.target.value; this.page = 1; this.load(); }
        });

        document.querySelectorAll('#machine-table th[data-sort]').forEach(th => {
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
            const data = await API.get('/api/machines', {
                keyword: this.keyword, sort: this.sort, order: this.order,
                page: this.page, size: this.size
            });
            this.render(data.list, data.total);
        } catch (e) { /* toast already shown */ }
    },

    render(list, total) {
        this.tableBody.innerHTML = '';
        if (!list || list.length === 0) {
            this.tableBody.innerHTML = '<tr><td colspan="5" class="empty-row">暂无数据</td></tr>';
        } else {
            list.forEach(m => {
                const statusClass = m.status === 'available' ? 'status-available' : 'status-maintenance';
                const statusText = m.status === 'available' ? '可用' : '维护中';
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>${escHtml(m.name)}</td>
                    <td>${escHtml(m.type || '-')}</td>
                    <td>${m.tokensPerGame}</td>
                    <td><span class="status-badge ${statusClass}">${statusText}</span></td>
                    <td>
                        <button class="btn-edit" data-id="${m.machineId}">编辑</button>
                        <button class="btn-del" data-id="${m.machineId}">删除</button>
                    </td>`;
                tr.querySelector('.btn-edit').addEventListener('click', () => this.showForm(m));
                tr.querySelector('.btn-del').addEventListener('click', () => this.confirmDelete(m.machineId));
                this.tableBody.appendChild(tr);
            });
        }

        this.pageInfo.textContent = `共 ${total} 条，第 ${this.page} 页`;
        this.prevBtn.disabled = this.page <= 1;
        this.nextBtn.disabled = this.page * this.size >= total;

        document.querySelectorAll('#machine-table th[data-sort]').forEach(th => {
            th.classList.remove('asc', 'desc');
            if (th.dataset.sort === this.sort) {
                th.classList.add(this.order);
            }
        });
    },

    showForm(machine) {
        const isEdit = !!machine;
        document.getElementById('modal-title').textContent = isEdit ? '编辑游戏机' : '新增游戏机';
        document.getElementById('modal-body').innerHTML = `
            <div class="form-group">
                <label>名称 <span style="color:red">*</span></label>
                <input id="form-name" value="${escHtml(machine ? machine.name : '')}">
            </div>
            <div class="form-group">
                <label>类型</label>
                <input id="form-type" value="${escHtml(machine ? (machine.type || '') : '')}" placeholder="如：赛车、射击">
            </div>
            <div class="form-group">
                <label>单次耗币</label>
                <input id="form-tokens" type="number" value="${machine ? (machine.tokensPerGame || 1) : 1}" min="1">
            </div>
            <div class="form-group">
                <label>状态</label>
                <select id="form-status">
                    <option value="available" ${machine && machine.status === 'available' ? 'selected' : ''}>可用</option>
                    <option value="in_maintenance" ${machine && machine.status === 'in_maintenance' ? 'selected' : ''}>维护中</option>
                </select>
            </div>`;

        Modal.show(async () => {
            const name = document.getElementById('form-name').value.trim();
            const type = document.getElementById('form-type').value.trim();
            const tokensPerGame = parseInt(document.getElementById('form-tokens').value) || 1;
            const status = document.getElementById('form-status').value;

            const body = { name, type, tokensPerGame, status };
            if (isEdit) {
                await API.put(`/api/machines/${machine.machineId}`, body);
            } else {
                await API.post('/api/machines', body);
            }
            Modal.hide();
            this.load();
        });
    },

    async confirmDelete(id) {
        if (!confirm('确定要删除该游戏机吗？')) return;
        try {
            await API.del(`/api/machines/${id}`);
            this.load();
        } catch (e) { /* toast already shown */ }
    }
};
