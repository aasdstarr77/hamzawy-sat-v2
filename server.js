const express = require('express');
const cors = require('cors');
const path = require('path');
const fs = require('fs');
const app = express();
const PORT = process.env.PORT || 3000;

app.use(cors());
app.use(express.json());
app.use(express.static('public'));

const DB_FILE = './orders.json';
if(!fs.existsSync(DB_FILE)) fs.writeFileSync(DB_FILE, '[]');

app.post('/api/orders', (req,res)=>{
  const orders = JSON.parse(fs.readFileSync(DB_FILE));
  const order = { id: Date.now(), ...req.body, date: new Date().toISOString(), status: 'جديد' };
  orders.unshift(order);
  fs.writeFileSync(DB_FILE, JSON.stringify(orders,null,2));
  console.log('طلب جديد:', order);
  res.json({ok:true, order});
});

app.get('/api/orders', (req,res)=>{
  const orders = JSON.parse(fs.readFileSync(DB_FILE));
  res.json(orders);
});

app.delete('/api/orders/:id', (req,res)=>{
  let orders = JSON.parse(fs.readFileSync(DB_FILE));
  orders = orders.filter(o=> o.id != req.params.id);
  fs.writeFileSync(DB_FILE, JSON.stringify(orders,null,2));
  res.json({ok:true});
});

app.listen(PORT, ()=> console.log('Admin running on '+PORT));
