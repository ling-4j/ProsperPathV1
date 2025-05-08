const { createProxyMiddleware } = require('http-proxy-middleware');
module.exports = function (app) {
  app.use(
    '/api/gold-price',
    createProxyMiddleware({
      target: 'https://edge-api.pnj.io',
      changeOrigin: true,
      pathRewrite: {
        '^/api/gold-price': '/ecom-frontend/v1/get-gold-price',
      },
      onProxyReq(proxyReq) {
        proxyReq.setHeader('Origin', 'https://edge-api.pnj.io');
      },
      onError(err, req, res) {
        res.writeHead(500, { 'Content-Type': 'text/plain' });
        res.end('Proxy error occurred.');
      },
    }),
  );
};
