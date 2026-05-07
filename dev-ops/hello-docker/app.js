const express = require('express');
const app = express();

app.get('/', (req, res) => {
  res.send('Hello from Express inside Docker!');
});

app.listen(3000, () => console.log('Server running on port 3000'));

// const http = require('http');

// const server = http.createServer((req, res) => {
//   res.end('Hello from inside Docker!');
// });

// server.listen(3000, () => console.log('Server running on port 3000'));
