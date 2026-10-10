(function () {
    function scrollChatToBottom() {
        const scroll = document.getElementById('chat-scroll');
        if (scroll) {
            scroll.scrollTop = scroll.scrollHeight;
        }
    }

    document.addEventListener('DOMContentLoaded', scrollChatToBottom);

    function storedCollapsed(id) {
        try {
            return localStorage.getItem('nav-collapsed:' + id) === 'true';
        } catch (e) {
            return false;
        }
    }

    function storeCollapsed(id, collapsed) {
        try {
            localStorage.setItem('nav-collapsed:' + id, String(collapsed));
        } catch (e) {
            // storage unavailable, the section just won't stay collapsed
        }
    }

    document.addEventListener('DOMContentLoaded', function () {
        document.querySelectorAll('.app-side-actions .app-side-link').forEach(function (link) {
            if (link.pathname === window.location.pathname) {
                link.classList.add('active');
            }
        });

        document.querySelectorAll('[data-remember-collapse]').forEach(function (section) {
            if (storedCollapsed(section.id)) {
                section.classList.remove('show');
                document.querySelectorAll('[data-bs-target="#' + section.id + '"]').forEach(function (toggle) {
                    toggle.classList.add('collapsed');
                    toggle.setAttribute('aria-expanded', 'false');
                });
            }
            section.addEventListener('shown.bs.collapse', function () {
                storeCollapsed(section.id, false);
            });
            section.addEventListener('hidden.bs.collapse', function () {
                storeCollapsed(section.id, true);
            });
        });
    });

    document.body.addEventListener('htmx:beforeRequest', function () {
        document.querySelectorAll('[data-alert]').forEach(function (alert) {
            alert.innerHTML = '';
        });
    });

    document.body.addEventListener('htmx:afterSwap', function (event) {
        if (event.detail.target.id === 'messages') {
            scrollChatToBottom();
        }
    });

    document.body.addEventListener('htmx:responseError', function () {
        const alert = document.getElementById('page-alert');
        if (alert) {
            alert.innerHTML = '<div class="alert alert-danger mb-3" role="alert">Request failed. Please try again.</div>';
        }
    });

    document.body.addEventListener('book-uploaded', function () {
        const modal = document.getElementById('upload-modal');
        if (modal) {
            bootstrap.Modal.getOrCreateInstance(modal).hide();
        }
        const form = document.getElementById('upload-form');
        if (form) {
            form.reset();
        }
    });

    // style inputs: a suggestion fills the text input next to the dropdown, "Custom" clears it for own text
    function syncStyleSuggestions(group, value) {
        const items = group.querySelectorAll('[data-style-suggestion]');
        const preset = Array.from(items).some(function (item) {
            return item.dataset.styleSuggestion !== '' && item.dataset.styleSuggestion === value;
        });
        items.forEach(function (item) {
            const custom = item.dataset.styleSuggestion === '';
            item.classList.toggle('active', custom ? value !== '' && !preset : item.dataset.styleSuggestion === value);
        });
    }

    document.body.addEventListener('click', function (event) {
        const suggestion = event.target.closest('[data-style-suggestion]');
        if (!suggestion) {
            return;
        }
        const group = suggestion.closest('.input-group');
        const input = group.querySelector('input');
        input.value = suggestion.dataset.styleSuggestion;
        syncStyleSuggestions(group, input.value);
        input.focus();
    });

    document.body.addEventListener('input', function (event) {
        const group = event.target.closest('.input-group');
        if (group && group.querySelector('[data-style-suggestion]')) {
            syncStyleSuggestions(group, event.target.value);
        }
    });

    document.body.addEventListener('keydown', function (event) {
        const textarea = event.target;
        if (textarea.id === 'chat-message' && event.key === 'Enter' && !event.shiftKey && !event.isComposing) {
            event.preventDefault();
            textarea.form.requestSubmit();
        }
    });
})();
