#!/usr/bin/env python3
"""Genera datos de ejemplo (clientes, mascotas, inventario, citas, facturas, pagos) para probar los reportes.
Uso: python3 sql/seed_reportes.py | docker exec -i clinica-db mysql -uroot -p12345 clinica
Es idempotente en lo posible: se puede volver a correr solo en una base sin estos datos."""
import random
from datetime import datetime, timedelta, date
random.seed(2026)
HOY = datetime(2026, 11, 21, 9, 0)
o = []
w = o.append
def q(s): return "'" + str(s).replace("'", "''") + "'"
def dt(d): return q(d.strftime('%Y-%m-%d %H:%M:%S'))

w("SET NAMES utf8mb4; SET @u = 3;")
# Razas
razas = [("Labrador", "Canino"), ("Criollo", "Canino"), ("Siamés", "Felino"), ("Poodle", "Canino"), ("Beagle", "Canino"), ("Persa", "Felino")]
for n, e in razas:
    w(f"INSERT INTO raza (nombre, especie, fecha_creacion) SELECT {q(n)}, {q(e)}, NOW() WHERE NOT EXISTS (SELECT 1 FROM raza WHERE nombre={q(n)});")
# Clientes y mascotas
nombres = [("Laura","Ramírez"),("Carlos","Moreno"),("Sofía","Castro"),("Andrés","Vargas"),("Paula","Ortiz"),("Jorge","Salazar"),("Valentina","Rojas"),("Camilo","Herrera")]
masc = ["Rocky","Toby","Nala","Simba","Coco","Max","Bella","Kira","Zeus","Mía","Rex","Lola","Thor","Chispa","Pelusa","Duke"]
cli_ids = [1, 2]
for i, (n, a) in enumerate(nombres):
    cid = 10 + i; cli_ids.append(cid)
    w(f"INSERT INTO cliente (cliente_id, tipo_documento, numero_documento, nombres, apellidos, fecha_nacimiento, genero, telefono, direccion, activo) "
      f"VALUES ({cid}, 'CC', {q(str(1010100000+i*137))}, {q(n)}, {q(a)}, '{1980+i}-0{1+i%9}-1{i%9}', {q('F' if i%2==0 else 'M')}, {q('3001234'+str(100+i))}, {q('Calle '+str(10+i)+' # 5-'+str(i))}, TRUE) ON DUPLICATE KEY UPDATE nombres=nombres;")
mascotas = {1: [1, 2], 2: [3]}  # cliente -> mascotas
mid = 10
for k, cid in enumerate(cli_ids[2:]):
    for j in range(2 if k % 2 == 0 else 1):
        mid += 1
        mascotas.setdefault(cid, []).append(mid)
        w(f"INSERT INTO mascota (mascota_id, nombre_mascota, edad, fecha_registro, raza_id, cliente_id) VALUES ({mid}, {q(masc[(mid-11)%len(masc)])}, {1+mid%9}, NOW(), "
          f"(SELECT raza_id FROM raza ORDER BY raza_id LIMIT 1 OFFSET {mid%7}), {cid}) ON DUPLICATE KEY UPDATE nombre_mascota=nombre_mascota;")
# Inventario: (id, codigo, nombre, tipo, compra, venta, iva, stock_min)
prods = [
 (6,'MED00006','Enrofloxacina 50mg','MEDICAMENTO',9000,15000,0,10),
 (7,'MED00007','Prednisolona 5mg','MEDICAMENTO',5000,9000,0,8),
 (8,'MED00008','Omeprazol 20mg','MEDICAMENTO',7000,12000,0,6),
 (9,'MED00009','Vacuna Triple Felina','MEDICAMENTO',30000,52000,19,5),
 (10,'MED00010','Vacuna Parvovirus','MEDICAMENTO',28000,48000,19,5),
 (11,'UTL00002','Guantes de látex (caja)','UTENSILIO',14000,22000,19,4),
 (12,'MED00012','Metronidazol 250mg','MEDICAMENTO',6500,11000,0,10),
]
# lotes por producto: (numero, inicial, disponible_final, vence en días desde HOY)
lotes = {
 6: [('L-ENR-001', 80, 55, 400)],
 7: [('L-PRE-001', 50, 6, 300)],            # stock mínimo (6 <= 8)
 8: [('L-OME-001', 40, 0, 200)],            # agotado
 9: [('L-VTF-001', 30, 21, 20), ('L-VTF-002', 20, 20, 500)],   # uno próximo a vencer
 10: [('L-VPA-001', 25, 14, 12)],           # próximo a vencer
 11: [('L-GUA-001', 20, 11, 700)],
 12: [('L-MET-001', 60, 38, -20), ('L-MET-002', 40, 36, 380)],  # un lote vencido
}
lote_id = 20
lotes_ids = {}
for p in prods:
    pid, cod, nom, tipo, comp, ven, iva, smin = p
    stock = sum(l[2] for l in lotes[pid])
    w(f"INSERT INTO medicamento (id, codigo, nombre, descripcion, presentacion, tipo, precio_compra, precio_venta, iva_porcentaje, stock_actual, stock_minimo, fecha_ingreso, activo, fecha_creacion_registro) "
      f"VALUES ({pid}, {q(cod)}, {q(nom)}, {q(nom)}, 'Unidad', {q(tipo)}, {comp}, {ven}, {iva}, {stock}, {smin}, '2026-09-15', TRUE, NOW()) ON DUPLICATE KEY UPDATE nombre=nombre;")
    lotes_ids[pid] = []
    for (num, ini, disp, dias) in lotes[pid]:
        lote_id += 1
        lotes_ids[pid].append((lote_id, disp, ini))
        venc = (HOY + timedelta(days=dias)).date()
        w(f"INSERT INTO lote_medicamento (id, medicamento_id, numero_lote, cantidad_inicial, cantidad_disponible, precio_compra, fecha_compra, fecha_vencimiento, fecha_creacion) "
          f"VALUES ({lote_id}, {pid}, {q(num)}, {ini}, {disp}, {comp}, '2026-09-15', '{venc}', NOW()) ON DUPLICATE KEY UPDATE numero_lote=numero_lote;")
        w(f"INSERT INTO movimiento_inventario (medicamento_id, lote_id, tipo, cantidad, fecha, usuario_id, motivo) VALUES ({pid}, {lote_id}, 'ENTRADA', {ini}, '2026-09-15 10:00:00', @u, 'Compra a proveedor');")
        if ini - disp > 0:  # salidas distribuidas en el tiempo
            resto = ini - disp; n = random.randint(2, 4)
            for s in range(n):
                c = resto // (n - s) if s < n - 1 else resto
                c = max(c, 1) if resto > 0 else 0
                if c <= 0: continue
                resto -= c
                f = HOY - timedelta(days=random.randint(2, 55), hours=random.randint(0, 6))
                w(f"INSERT INTO movimiento_inventario (medicamento_id, lote_id, tipo, cantidad, fecha, usuario_id, motivo) VALUES ({pid}, {lote_id}, 'SALIDA', {c}, {dt(f)}, 7, 'Dispensación a paciente');")

# Citas, atenciones, facturas, pagos
medico_esp = {1: 1, 2: 2, 3: 1}
tarifas = {1: [(1,60000,0),(2,45000,0)], 2: [(3,90000,19),(4,250000,19)]}
tarifas_iva = {1:19, 2:19, 3:19, 4:19}
estados_w = ['FINALIZADA']*20 + ['ATENDIDA']*5 + ['CANCELADA']*6 + ['PENDIENTE']*8
motivos = ["Control general","Vacunación","Vómito y diarrea","Cojera","Revisión de piel","Control postoperatorio","Desparasitación","Dolor de oído","Chequeo anual"]
cita = 10; ate = 10; fac = 10; usados = set()
mascs = [(c, m) for c, ms in mascotas.items() for m in ms]
dias = list(range(-60, 8))
for _ in range(60):
    d = HOY + timedelta(days=random.choice(dias))
    if d.weekday() == 6: continue
    h = random.choice([8,9,10,11,14,15,16,17]); med = random.choice([1,2,3])
    inicio = d.replace(hour=h, minute=random.choice([0,30]))
    if (med, inicio) in usados: continue
    usados.add((med, inicio))
    pasado = inicio < HOY
    est = random.choice(estados_w) if pasado else random.choice(['PENDIENTE']*4 + ['CANCELADA'])
    if pasado and est == 'PENDIENTE': est = 'FINALIZADA'
    c, m = random.choice(mascs); esp = medico_esp[med]; cita += 1
    canc = q("El propietario no pudo asistir") if est == 'CANCELADA' else 'NULL'
    w(f"INSERT INTO cita (id, cliente_id, mascota_id, medico_id, fecha_hora, estado, motivo, fecha_hora_fin, motivo_cancelacion, especializacion_id) "
      f"VALUES ({cita}, {c}, {m}, {med}, {dt(inicio)}, {q(est)}, {q(random.choice(motivos))}, {dt(inicio+timedelta(minutes=30))}, {canc}, {esp});")
    if est in ('ATENDIDA','FINALIZADA'):
        ate += 1
        tf = random.choice(tarifas[esp]); tid, val, _ = tf; iva = tarifas_iva[tid]
        w(f"INSERT INTO atencion_medica (id, cita_id, medico_id, tarifa_id, motivo, sintomas, diagnostico, observaciones, recomendaciones, estado, fecha_inicio, fecha_fin) "
          f"VALUES ({ate}, {cita}, {med}, {tid}, 'Consulta', 'Decaimiento', 'Cuadro leve', 'Sin novedades', 'Control en 15 días', {q('FINALIZADA' if est=='FINALIZADA' else 'EN_CURSO')}, {dt(inicio)}, {dt(inicio+timedelta(minutes=30)) if est=='FINALIZADA' else 'NULL'});")
    if est == 'FINALIZADA':
        fac += 1
        lineas = [('SERVICIO', f"Tarifa {tid}", tid, 'NULL', 1, val, iva)]
        if random.random() < 0.6:
            pid = random.choice([1,2,6,7,9,10,12]); pv = {1:12000,2:24000,6:15000,7:9000,9:52000,10:48000,12:11000}[pid]; pi = {9:19,10:19}.get(pid,0)
            lineas.append(('MEDICAMENTO', f"Producto {pid}", 'NULL', pid, random.randint(1,3), pv, pi))
        sub_ = sum(l[4]*l[5] for l in lineas); imp = sum(round(l[4]*l[5]*l[6]/100,2) for l in lineas)
        desc = round(sub_*0.1, 2) if random.random() < 0.2 else 0
        tot = round(sub_ + imp - desc, 2)
        r = random.random(); ep = 'PAGADA' if r < 0.6 else 'PARCIAL' if r < 0.75 else 'PENDIENTE' if r < 0.92 else 'ANULADA'
        anul = (f"'Factura emitida por error', {dt(inicio+timedelta(hours=2))}" if ep == 'ANULADA' else "NULL, NULL")
        w(f"INSERT INTO factura (id, numero, cita_id, atencion_id, cliente_id, mascota_id, medico_id, especializacion_id, subtotal, impuestos, descuento, total, fecha, estado_pago, motivo_anulacion, fecha_anulacion, usuario_id) "
          f"VALUES ({fac}, {q('FAC-%d-%06d' % (inicio.year, fac))}, {cita}, {ate}, {c}, {m}, {med}, {esp}, {sub_}, {imp}, {desc}, {tot}, {dt(inicio+timedelta(minutes=40))}, {q(ep)}, {anul}, 4);")
        for (t, dsc, tfid, mid_, cant, pu, iv) in lineas:
            s_ = cant*pu
            w(f"INSERT INTO factura_detalle (factura_id, tipo, descripcion, tarifa_id, medicamento_id, cantidad, precio_unitario, iva_porcentaje, subtotal, iva_valor) "
              f"VALUES ({fac}, {q(t)}, {q((dsc if t=='MEDICAMENTO' else 'Servicio veterinario'))}, {tfid}, {mid_}, {cant}, {pu}, {iv}, {s_}, {round(s_*iv/100,2)});")
        if ep in ('PAGADA','PARCIAL'):
            valor = tot if ep == 'PAGADA' else round(tot*0.5, 2)
            w(f"INSERT INTO pago (factura_id, valor, metodo, referencia, fecha, usuario_id) VALUES ({fac}, {valor}, {q(random.choice(['EFECTIVO','TARJETA','TRANSFERENCIA']))}, {q('REF-%05d' % fac)}, {dt(inicio+timedelta(minutes=50))}, 4);")
w("UPDATE medicamento m SET fecha_modificacion_registro = NOW() WHERE id >= 6;")
print("\n".join(o))
