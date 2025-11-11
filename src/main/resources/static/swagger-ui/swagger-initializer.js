window.onload = function() {
  const original = window.ui;

  const checkExist = setInterval(() => {
    if (window.ui && window.ui.getConfigs) {
      clearInterval(checkExist);

      const config = window.ui.getConfigs();
      const newConfig = {
        ...config,
        requestInterceptor: (req) => {
          try {
            const auths = window.ui.authSelectors.authorizations() || {};
            const oauth = auths['oauth2'];

            if (oauth && oauth.token && oauth.token.id_token) {
              req.headers['Authorization'] = 'Bearer ' + oauth.token.id_token;

            }
          } catch (e) {
            console.warn('Could not inject ID token:', e);
          }
          return req;
        }
      };

      window.ui = SwaggerUIBundle(newConfig);
    }
  }, 500);
};
