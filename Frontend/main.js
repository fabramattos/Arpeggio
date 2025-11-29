document.addEventListener('DOMContentLoaded', () => {
    const searchBtn = document.getElementById('searchBtn');
    const artistInput = document.getElementById('artistInput');
    const resultsSection = document.getElementById('resultsSection');
    const resultsGrid = document.getElementById('resultsGrid');
    const queryTerm = document.getElementById('queryTerm');

    searchBtn.addEventListener('click', handleSearch);
    artistInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') handleSearch();
    });

    function handleSearch() {
        const query = artistInput.value.trim();
        if (!query) return;

        // Show loading state (optional enhancement)
        searchBtn.textContent = 'Buscando...';
        searchBtn.disabled = true;

        // Simulate API call
        fetchData(query)
            .then(data => {
                displayResults(data);
            })
            .catch(err => {
                console.error('Erro:', err);
                alert('Ocorreu um erro ao buscar os dados.');
            })
            .finally(() => {
                searchBtn.textContent = 'Comparar';
                searchBtn.disabled = false;
            });
    }

    // Mock API function
    function fetchData(artist) {
        return new Promise((resolve) => {
            setTimeout(() => {
                resolve({
                    "busca": artist,
                    "resultados": [
                        {
                            "streaming": "Youtube Music",
                            "consulta": artist,
                            "erro": "Serviço temporariamente desabilitado"
                        },
                        {
                            "streaming": "Tidal",
                            "consulta": artist,
                            "erro": "org.springframework.web.reactive.function.client.WebClientResponseException$NotFound: 404 Not Found"
                        },
                        {
                            "streaming": "Spotify",
                            "consulta": artist,
                            "albuns": 73
                        },
                        {
                            "streaming": "Deezer",
                            "consulta": artist,
                            "albuns": 75
                        }
                    ]
                });
            }, 800); // Simulate network delay
        });
    }

    function displayResults(data) {
        queryTerm.textContent = data.busca;
        resultsGrid.innerHTML = '';
        resultsSection.classList.remove('hidden');

        data.resultados.forEach(result => {
            const card = document.createElement('div');
            card.className = 'result-card';

            const title = document.createElement('div');
            title.className = 'streaming-name';
            title.textContent = result.streaming;

            card.appendChild(title);

            if (result.erro) {
                card.classList.add('error');
                const errorMsg = document.createElement('div');
                errorMsg.className = 'error-msg';
                // Simplify error message for display if it's too long/technical
                if (result.erro.includes('404')) {
                    errorMsg.textContent = 'Artista não encontrado';
                } else {
                    errorMsg.textContent = 'Indisponível no momento';
                }
                card.appendChild(errorMsg);
            } else {
                const count = document.createElement('div');
                count.className = 'album-count';
                count.textContent = result.albuns;

                const label = document.createElement('div');
                label.style.fontSize = '0.9rem';
                label.style.color = 'var(--text-secondary)';
                label.textContent = 'Álbuns';

                card.appendChild(count);
                card.appendChild(label);
            }

            resultsGrid.appendChild(card);
        });

        // Scroll to results
        resultsSection.scrollIntoView({ behavior: 'smooth' });
    }
});
