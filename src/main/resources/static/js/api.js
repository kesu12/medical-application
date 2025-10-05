(function(){
  const STORAGE = {
    accessTokenKey: 'accessToken',
    refreshTokenKey: 'refreshToken',
    tokenTypeKey: 'tokenType'
  };

  function getAccessToken(){ return localStorage.getItem(STORAGE.accessTokenKey); }
  function getRefreshToken(){ return localStorage.getItem(STORAGE.refreshTokenKey); }
  function getTokenType(){ return localStorage.getItem(STORAGE.tokenTypeKey) || 'Bearer'; }
  function isLoggedIn(){ return !!getAccessToken(); }

  function setTokens({ accessToken, refreshToken, tokenType }){
    if (accessToken) localStorage.setItem(STORAGE.accessTokenKey, accessToken);
    if (refreshToken) localStorage.setItem(STORAGE.refreshTokenKey, refreshToken);
    if (tokenType) localStorage.setItem(STORAGE.tokenTypeKey, tokenType);
  }

  function clearTokens(){
    localStorage.removeItem(STORAGE.accessTokenKey);
    localStorage.removeItem(STORAGE.refreshTokenKey);
    localStorage.removeItem(STORAGE.tokenTypeKey);
  }

  function authHeaders(){
    const token = getAccessToken();
    if (!token) return {};
    return { 'Authorization': `${getTokenType()} ${token}` };
  }

  async function apiFetch(path, { method = 'GET', headers = {}, body } = {}){
    const res = await fetch(path, {
      method,
      headers: { 'Content-Type': 'application/json', ...headers, ...authHeaders() },
      body: body ? JSON.stringify(body) : undefined
    });
    if (res.status === 401 && getRefreshToken()) {
      const refreshed = await tryRefresh();
      if (refreshed) {
        const retry = await fetch(path, {
          method,
          headers: { 'Content-Type': 'application/json', ...headers, ...authHeaders() },
          body: body ? JSON.stringify(body) : undefined
        });
        return retry;
      }
    }
    return res;
  }

  async function tryRefresh(){
    const refreshToken = getRefreshToken();
    if (!refreshToken) return false;
    const res = await fetch('/api/auth/refresh', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken })
    });
    if (!res.ok) return false;
    const data = await res.json();
    if (data && data.accessToken) {
      setTokens({ accessToken: data.accessToken, refreshToken: data.refreshToken, tokenType: data.tokenType });
      return true;
    }
    return false;
  }

  async function logout(){
    const refreshToken = getRefreshToken();
    try {
      if (refreshToken) {
        await fetch('/api/auth/logout', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ refreshToken })
        });
      }
    } finally {
      clearTokens();
    }
  }

  async function getMe(){
    const res = await apiFetch('/api/auth/me');
    if (!res.ok) throw new Error('Failed to fetch profile');
    return res.json();
  }

  window.AppApi = {
    isLoggedIn,
    setTokens,
    clearTokens,
    logout,
    getMe,
    apiGet: (path) => apiFetch(path),
    apiPost: (path, body) => apiFetch(path, { method: 'POST', body }),
    apiPatch: (path, body) => apiFetch(path, { method: 'PATCH', body }),
    apiDelete: (path) => apiFetch(path, { method: 'DELETE' }),
  };
})();