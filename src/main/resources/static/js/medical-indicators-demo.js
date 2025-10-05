const exampleMobileData = {
    "heartrate": 75,
    "temperature": 36.8,
    "spo2": 98,
    "patientId": 12345
};

async function submitMedicalIndicators(indicators) {
    try {
        const response = await fetch('/api/medical-indicators/submit', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(indicators)
        });
        
        const result = await response.json();
        console.log('Response from server:', result);
        return result;
    } catch (error) {
        console.error('Error submitting indicators:', error);
        throw error;
    }
}

async function generateRandomIndicators(includeCritical = false) {
    try {
        const response = await fetch(`/api/medical-indicators/generate-random?includeCritical=${includeCritical}`);
        const indicators = await response.json();
        console.log('Generated indicators:', indicators);
        return indicators;
    } catch (error) {
        console.error('Error generating indicators:', error);
        throw error;
    }
}

async function analyzeIndicators(indicators) {
    try {
        const response = await fetch('/api/medical-indicators/analyze', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(indicators)
        });
        
        const analysis = await response.json();
        console.log('Analysis result:', analysis);
        return analysis;
    } catch (error) {
        console.error('Error analyzing indicators:', error);
        throw error;
    }
}

async function getLatestIndicators(patientId) {
    try {
        const response = await fetch(`/api/medical-indicators/patient/${patientId}/latest`);
        const indicators = await response.json();
        console.log('Latest indicators:', indicators);
        return indicators;
    } catch (error) {
        console.error('Error getting latest indicators:', error);
        throw error;
    }
}

function demonstrateNormalIndicators() {
    console.log('=== Демонстрация нормальных показателей ===');
    const normalIndicators = {
        heartrate: 72,
        temperature: 36.6,
        spo2: 99,
        patientId: 1001
    };
    
    submitMedicalIndicators(normalIndicators)
        .then(result => {
            console.log('Нормальные показатели отправлены:', result);
            return analyzeIndicators(normalIndicators);
        })
        .then(analysis => {
            console.log('Анализ нормальных показателей:', analysis);
        });
}

function demonstrateCriticalIndicators() {
    console.log('=== Демонстрация критических показателей ===');
    const criticalIndicators = {
        heartrate: 45,
        temperature: 39.2,
        spo2: 88,
        patientId: 1002
    };
    
    submitMedicalIndicators(criticalIndicators)
        .then(result => {
            console.log('Критические показатели отправлены:', result);
            return analyzeIndicators(criticalIndicators);
        })
        .then(analysis => {
            console.log('Анализ критических показателей:', analysis);
        });
}

function demonstrateRandomGeneration() {
    console.log('=== Демонстрация генерации случайных показателей ===');
    
    generateRandomIndicators(false)
        .then(indicators => {
            console.log('Сгенерированы нормальные показатели:', indicators);
            return submitMedicalIndicators(indicators);
        })
        .then(result => {
            console.log('Результат отправки:', result);
        });
    
    setTimeout(() => {
        generateRandomIndicators(true)
            .then(indicators => {
                console.log('Сгенерированы показатели (возможно критические):', indicators);
                return submitMedicalIndicators(indicators);
            })
            .then(result => {
                console.log('Результат отправки:', result);
            });
    }, 1000);
}

if (typeof module !== 'undefined' && module.exports) {
    module.exports = {
        submitMedicalIndicators,
        generateRandomIndicators,
        analyzeIndicators,
        getLatestIndicators,
        demonstrateNormalIndicators,
        demonstrateCriticalIndicators,
        demonstrateRandomGeneration
    };
}

if (typeof window !== 'undefined') {
    console.log('Медицинские показатели API готов к использованию');
    console.log('Пример данных от мобильного приложения:', exampleMobileData);
}