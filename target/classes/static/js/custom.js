// WebSocket connection handling
let stompClient = null;

function connectWebSocket() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    
    stompClient.connect({}, function(frame) {
        // Subscribe to personal notifications
        const username = document.getElementById('currentUsername').value;
        stompClient.subscribe('/user/' + username + '/queue/notifications', onNotification);
        
        // Subscribe to group notifications if in a group page
        const groupId = document.getElementById('groupId')?.value;
        if (groupId) {
            stompClient.subscribe('/topic/group.' + groupId, onGroupUpdate);
        }
    });
}

// Image handling functions
function handleImageUpload(files, previewContainer, callback) {
    Array.from(files).forEach(file => {
        // Create FormData and send to server
        const formData = new FormData();
        formData.append('file', file);
        
        fetch('/api/images/upload', {
            method: 'POST',
            body: formData
        })
        .then(response => response.json())
        .then(image => {
            // Add preview
            addImagePreview(file, previewContainer);
            // Call callback with image data
            if (callback) callback(image);
        })
        .catch(error => console.error('Error uploading image:', error));
    });
}

function addImagePreview(file, container) {
    const reader = new FileReader();
    reader.onload = function(e) {
        const preview = document.createElement('div');
        preview.className = 'position-relative d-inline-block';
        preview.innerHTML = `
            <img src="${e.target.result}" class="image-preview">
            <button type="button" class="btn-close position-absolute top-0 end-0 m-1" 
                    onclick="this.parentElement.remove()"></button>
        `;
        container.appendChild(preview);
    }
    reader.readAsDataURL(file);
}

// Group functionality
function searchUsers(query) {
    return fetch(`/api/users/search?q=${encodeURIComponent(query)}`)
        .then(response => response.json());
}

function inviteToGroup(userId, groupId) {
    return fetch(`/api/groups/${groupId}/members/${userId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        }
    });
}

// Notification handling
function onNotification(message) {
    const notification = JSON.parse(message.body);
    showToast(notification.title, notification.message);
    updateNotificationBadge();
}

function onGroupUpdate(message) {
    const update = JSON.parse(message.body);
    if (update.type === 'NEW_POST') {
        prependNewPost(update.data);
    } else if (update.type === 'MEMBER_UPDATE') {
        updateMembersList(update.data);
    }
}

function showToast(title, message) {
    const toastContainer = document.getElementById('toastContainer');
    if (!toastContainer) {
        const container = document.createElement('div');
        container.id = 'toastContainer';
        container.className = 'toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = 'toast';
    toast.innerHTML = `
        <div class="toast-header">
            <strong class="me-auto">${title}</strong>
            <button type="button" class="btn-close" data-bs-dismiss="toast"></button>
        </div>
        <div class="toast-body">${message}</div>
    `;
    
    toastContainer.appendChild(toast);
    const bsToast = new bootstrap.Toast(toast);
    bsToast.show();
    
    toast.addEventListener('hidden.bs.toast', () => toast.remove());
}

// Initialize functionality
document.addEventListener('DOMContentLoaded', function() {
    // Connect to WebSocket
    connectWebSocket();
    
    // Initialize image upload listeners
    const imageInputs = document.querySelectorAll('input[type="file"][accept="image/*"]');
    imageInputs.forEach(input => {
        const previewContainer = document.getElementById(input.dataset.previewContainer);
        input.addEventListener('change', (e) => handleImageUpload(e.target.files, previewContainer));
    });
    
    // Initialize user search in group pages
    const userSearchInput = document.getElementById('userSearch');
    if (userSearchInput) {
        userSearchInput.addEventListener('input', debounce(function() {
            if (this.value.length < 2) return;
            
            searchUsers(this.value)
                .then(users => {
                    const resultsContainer = document.getElementById('userSearchResults');
                    resultsContainer.innerHTML = users.map(user => `
                        <div class="d-flex justify-content-between align-items-center p-2 border-bottom">
                            <span>${user.username}</span>
                            <button onclick="inviteToGroup(${user.id}, ${groupId})" 
                                    class="btn btn-sm btn-primary">Invite</button>
                        </div>
                    `).join('');
                });
        }, 300));
    }
});

// Utility functions
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func.apply(this, args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}