// كود عرض الطلبات في صفحة الأدمن - حطه في public/app.js لو عايز
async function loadOrders(){
  const res = await fetch('/api/orders');
  const orders = await res.json();
  const div = document.getElementById('orders');
  if(!div) return;
  div.innerHTML = orders.map(o=>`
    <div style="background:#1e293b;padding:10px;margin:5px;border-radius:8px">
      <b>${o.service}</b> - ${o.phone}<br>${o.address}<br><small>${o.details||''}</small><br>
      <small>${new Date(o.date).toLocaleString('ar-EG')}</small>
      <button onclick="del(${o.id})">مسح</button>
    </div>
  `).join('');
}
async function del(id){ await fetch('/api/orders/'+id,{method:'DELETE'}); loadOrders(); }
setInterval(loadOrders,3000); loadOrders();
