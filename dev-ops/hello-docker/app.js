const express = require('express');
const mongoose = require('mongoose');

const app = express();

mongoose.connect('mongodb://mongo:27017/hellodb')
  .then(() => console.log('Connected to MongoDB'))
  .catch(err => console.error('MongoDB error:', err));

app.get('/', (req, res) => {
  const status = mongoose.connection.readyState === 1 ? 'Connected ✅' : 'Disconnected ❌';
  res.send(`Hello from Express! MongoDB: ${status}`);
});

app.listen(3000, () => console.log('Server running on port 3000'));


// const express = require('express');
// const app = express();

// app.get('/', (req, res) => {
//   res.send('Hello from Express inside Docker!!!!');
// });

// app.listen(3000, () => console.log('Server running on port 3000'));

// // const http = require('http');

// // const server = http.createServer((req, res) => {
// //   res.end('Hello from inside Docker!');
// // });

// // server.listen(3000, () => console.log('Server running on port 3000'));
