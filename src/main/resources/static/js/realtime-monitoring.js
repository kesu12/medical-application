class RealtimeMedicalMonitoring {
    constructor(patientId) {
        this.patientId = patientId;
        this.stompClient = null;
        this.isConnected = false;
        this.isMonitoring = false;
        this.currentIndicators = null;
        this.alertHistory = [];
        
        this.initializeWebSocket();
        this.setupEventListeners();
    }

    initializeWebSocket() {
        const socket = new SockJS('/ws');
        this.stompClient = Stomp.over(socket);
        
        this.stompClient.connect({}, (frame) => {
            console.log('Connected to WebSocket: ' + frame);
            this.isConnected = true;
            this.updateConnectionStatus(true);
            this.hideLoadingIndicator();
            
            this.stompClient.subscribe('/topic/medical-indicators/' + this.patientId, (message) => {
                const indicators = JSON.parse(message.body);
                this.handleNewIndicators(indicators);
            });
            
        }, (error) => {
            console.error('WebSocket connection error:', error);
            this.isConnected = false;
            this.updateConnectionStatus(false);
            this.hideLoadingIndicator();
            this.showErrorMessage('Не удалось подключиться к WebSocket: ' + error);
        });
    }

    setupEventListeners() {
        const startBtn = document.getElementById('getIndicators');
        if (startBtn) {
            startBtn.addEventListener('click', () => this.startMonitoring());
        }

        const stopBtn = document.getElementById('stopMonitoring');
        if (stopBtn) {
            stopBtn.addEventListener('click', () => this.stopMonitoring());
        }

        const testBtn = document.getElementById('sendTestIndicators');
        if (testBtn) {
            testBtn.addEventListener('click', () => this.sendTestIndicators());
        }

        const reloadBtn = document.getElementById('reloadWebSocket');
        if (reloadBtn) {
            reloadBtn.addEventListener('click', () => this.reloadWebSocket());
        }
    }

    startMonitoring() {
        if (!this.isConnected) {
            alert('WebSocket не подключен!');
            return;
        }

        if (this.isMonitoring) {
            console.log('Monitoring already started');
            return;
        }

        this.stompClient.send('/app/start-monitoring', {}, this.patientId);
        this.isMonitoring = true;
        this.updateMonitoringStatus(true);
        
        // Переключаем кнопки
        const getIndicatorsBtn = document.getElementById('getIndicators');
        const stopBtn = document.getElementById('stopMonitoring');
        if (getIndicatorsBtn) getIndicatorsBtn.style.display = 'none';
        if (stopBtn) stopBtn.style.display = 'inline-block';
        
        console.log('Started monitoring for patient:', this.patientId);
    }

    stopMonitoring() {
        if (!this.isMonitoring) {
            console.log('Monitoring not started');
            return;
        }

        this.stompClient.send('/app/stop-monitoring', {}, this.patientId);
        
        this.isMonitoring = false;
        this.updateMonitoringStatus(false);
        
        this.clearIndicators();
        
        // Переключаем кнопки обратно
        const getIndicatorsBtn = document.getElementById('getIndicators');
        const stopBtn = document.getElementById('stopMonitoring');
        if (getIndicatorsBtn) getIndicatorsBtn.style.display = 'inline-block';
        if (stopBtn) stopBtn.style.display = 'none';
        
        console.log('Stopped monitoring for patient:', this.patientId);
    }

    sendTestIndicators() {
        if (!this.isConnected) {
            alert('WebSocket не подключен!');
            return;
        }

        this.stompClient.send('/app/send-test-indicators', {}, this.patientId);
        console.log('Sent test indicators for patient:', this.patientId);
    }

    reloadWebSocket() {
        console.log('Перезагрузка WebSocket соединения...');
        
        const wasMonitoring = this.isMonitoring;
        
        this.disconnect();
        
        this.clearIndicators();
        
        this.showLoadingIndicator();
        
        setTimeout(() => {
            try {
                this.initializeWebSocket();
                
                if (wasMonitoring) {
                    setTimeout(() => {
                        if (this.isConnected) {
                            this.startMonitoring();
                        }
                    }, 1000);
                }
                
                console.log('WebSocket соединение перезагружено');
            } catch (error) {
                console.error('Ошибка при перезагрузке WebSocket:', error);
                this.hideLoadingIndicator();
                this.showErrorMessage('Ошибка перезагрузки WebSocket: ' + error.message);
            }
        }, 1000);
    }

    handleNewIndicators(indicators) {
        this.currentIndicators = indicators;
        
        this.updateIndicatorsDisplay(indicators);
        
        this.checkForAlerts(indicators);
        
        this.updateChart(indicators);
        
        console.log('New indicators received:', indicators);
    }

    updateIndicatorsDisplay(indicators) {
        this.updateElement('heartrate-value', indicators.heartrate + ' уд/мин');
        this.updateElement('temperature-value', indicators.temperature + '°C');
        this.updateElement('spo2-value', indicators.spo2 + '%');
        this.updateElement('timestamp-value', new Date(indicators.timestamp).toLocaleTimeString());
        
        const category = this.getCategory(indicators);
        this.updateElement('status-value', category);
        this.updateElement('status-description', this.getStatusDescription(category));
        
        this.updateStatusColor(category);
    }

    checkForAlerts(indicators) {
        const category = this.getCategory(indicators);
        
        if (category !== 'Normal') {
            this.showAlert(indicators, category);
            this.addToAlertHistory(indicators, category);
        }
    }

    showAlert(indicators, category) {
        const alertLevel = this.getAlertLevel(category);
        const message = this.getAlertMessage(indicators, category);
        
        const alertDiv = document.createElement('div');
        alertDiv.className = `alert alert-${alertLevel.toLowerCase()} alert-dismissible fade show`;
        alertDiv.innerHTML = `
            <strong>${alertLevel}:</strong> ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        `;
        
        const alertsContainer = document.getElementById('alerts-container');
        if (alertsContainer) {
            alertsContainer.appendChild(alertDiv);
            
            setTimeout(() => {
                if (alertDiv.parentNode) {
                    alertDiv.remove();
                }
            }, 10000);
        }
        
        this.playAlertSound(alertLevel);
    }

    addToAlertHistory(indicators, category) {
        const alert = {
            timestamp: new Date(indicators.timestamp),
            category: category,
            indicators: indicators,
            message: this.getAlertMessage(indicators, category)
        };
        
        this.alertHistory.unshift(alert);
        
        if (this.alertHistory.length > 50) {
            this.alertHistory = this.alertHistory.slice(0, 50);
        }
        
        this.updateAlertHistory();
    }

    updateAlertHistory() {
        const historyContainer = document.getElementById('alert-history');
        if (!historyContainer) return;
        
        historyContainer.innerHTML = '';
        
        this.alertHistory.forEach(alert => {
            const alertItem = document.createElement('div');
            alertItem.className = 'alert-item';
            alertItem.innerHTML = `
                <div class="alert-time">${alert.timestamp.toLocaleTimeString()}</div>
                <div class="alert-category">${alert.category}</div>
                <div class="alert-message">${alert.message}</div>
            `;
            historyContainer.appendChild(alertItem);
        });
    }

    updateChart(indicators) {
        console.log('Chart update:', indicators);
    }

    updateElement(id, value) {
        const element = document.getElementById(id);
        if (element) {
            element.textContent = value;
        }
    }

    updateConnectionStatus(connected) {
        const statusElement = document.getElementById('connection-status');
        if (statusElement) {
            statusElement.textContent = connected ? 'Подключен' : 'Отключен';
            statusElement.className = connected ? 'status-connected' : 'status-disconnected';
        }
    }

    updateMonitoringStatus(monitoring) {
        const statusElement = document.getElementById('monitoring-status');
        if (statusElement) {
            statusElement.textContent = monitoring ? 'Активен' : 'Остановлен';
            statusElement.className = monitoring ? 'status-active' : 'status-inactive';
        }
    }

    updateStatusColor(category) {
        const statusElement = document.getElementById('status-indicator');
        if (statusElement) {
            statusElement.className = `status-indicator status-${category.toLowerCase().replace(' ', '-')}`;
        }
    }

    getCategory(indicators) {
        const temperatureNormal = indicators.temperature >= 35.0 && indicators.temperature <= 37.0;
        
        const heartrateNormal = indicators.heartrate >= 60 && indicators.heartrate <= 100;
        
        const spo2Normal = indicators.spo2 >= 96 && indicators.spo2 <= 100;
        
        if (temperatureNormal && heartrateNormal && spo2Normal) {
            return 'Normal';
        }
        
        return 'Warning';
    }

    getStatusDescription(category) {
        const descriptions = {
            'Normal': 'Все показатели в норме',
            'Warning': 'Требует внимания врача'
        };
        return descriptions[category] || 'Неизвестный статус';
    }

    getAlertLevel(category) {
        const levels = {
            'Normal': 'INFO',
            'Warning': 'WARNING'
        };
        return levels[category] || 'INFO';
    }

    getAlertMessage(indicators, category) {
        const messages = [];
        
        if (indicators.temperature < 35.0) {
            messages.push(`Низкая температура: ${indicators.temperature}°C (норма: 35.0-37.0°C)`);
        } else if (indicators.temperature > 37.0) {
            messages.push(`Высокая температура: ${indicators.temperature}°C (норма: 35.0-37.0°C)`);
        }
        
        if (indicators.heartrate < 60) {
            messages.push(`Низкий пульс: ${indicators.heartrate} уд/мин (норма: 60-100 уд/мин)`);
        } else if (indicators.heartrate > 100) {
            messages.push(`Высокий пульс: ${indicators.heartrate} уд/мин (норма: 60-100 уд/мин)`);
        }
        
        if (indicators.spo2 < 96) {
            messages.push(`Низкий SpO2: ${indicators.spo2}% (норма: 96-100%)`);
        }
        
        return messages.join('. ');
    }

    playAlertSound(alertLevel) {
        if (alertLevel === 'WARNING') {
            console.log('Playing alert sound for level:', alertLevel);
        }
    }

    clearIndicators() {
        this.updateElement('heartrate-value', '--');
        this.updateElement('temperature-value', '--');
        this.updateElement('spo2-value', '--');
        this.updateElement('timestamp-value', '--');
        this.updateElement('status-value', 'Норма');
        this.updateElement('status-description', 'Мониторинг остановлен');
        
        const alertsContainer = document.getElementById('alerts-container');
        if (alertsContainer) {
            alertsContainer.innerHTML = '<div class="text-muted text-center">Нет активных уведомлений</div>';
        }
        
        this.updateStatusColor('Normal');
    }

    disconnect() {
        if (this.isMonitoring) {
            this.stopMonitoring();
        }
        
        if (this.stompClient) {
            this.stompClient.disconnect();
        }
        
        this.isConnected = false;
        this.isMonitoring = false;
        this.updateConnectionStatus(false);
        
        console.log('Disconnected from WebSocket');
    }

    showLoadingIndicator() {
        this.updateElement('connection-status', 'Переподключение...');
        this.updateElement('monitoring-status', 'Перезагрузка');
        
        const reloadBtn = document.getElementById('reloadWebSocket');
        if (reloadBtn) {
            reloadBtn.disabled = true;
            reloadBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Переподключение...';
        }
    }

    hideLoadingIndicator() {
        const reloadBtn = document.getElementById('reloadWebSocket');
        if (reloadBtn) {
            reloadBtn.disabled = false;
            reloadBtn.innerHTML = '<i class="fas fa-sync-alt"></i> Перезагрузить WebSocket';
        }
    }

    showErrorMessage(message) {
        this.updateElement('connection-status', 'Ошибка подключения');
        
        const alertsContainer = document.getElementById('alerts-container');
        if (alertsContainer) {
            const errorAlert = document.createElement('div');
            errorAlert.className = 'alert alert-danger alert-dismissible fade show';
            errorAlert.innerHTML = `
                <strong>Ошибка:</strong> ${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            `;
            alertsContainer.appendChild(errorAlert);
            
            setTimeout(() => {
                if (errorAlert.parentNode) {
                    errorAlert.remove();
                }
            }, 10000);
        }
        
        console.error('Error message shown:', message);
    }
}

let medicalMonitoring = null;

document.addEventListener('DOMContentLoaded', function() {
    const patientId = getPatientIdFromPage();
    
    if (patientId) {
        medicalMonitoring = new RealtimeMedicalMonitoring(patientId);
    }
});

window.addEventListener('beforeunload', function() {
    if (medicalMonitoring) {
        medicalMonitoring.disconnect();
    }
});

document.addEventListener('visibilitychange', function() {
    if (document.hidden && medicalMonitoring) {
        if (medicalMonitoring.isMonitoring) {
            medicalMonitoring.stopMonitoring();
        }
    }
});

function getPatientIdFromPage() {
    const patientIdElement = document.getElementById('patient-id');
    if (patientIdElement) {
        return parseInt(patientIdElement.textContent);
    }
    
    const urlParams = new URLSearchParams(window.location.search);
    const patientId = urlParams.get('patientId');
    if (patientId) {
        return parseInt(patientId);
    }
    
    return 1001;
}