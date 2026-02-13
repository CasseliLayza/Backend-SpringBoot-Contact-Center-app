![logo](resources/static/assets/img/CC-Spring.jpg)
# 🚀 IVR Banking System Enterprise – Asterisk + Spring Boot + AMI

### Plataforma Inteligente de Validación, Procesamiento y Registro de Clientes en Tiempo Real

Sistema IVR integrado con **Asterisk (PJSIP)** y **Spring Boot** usando **AMI (Asterisk Manager Interface)** para:

* 📲 Recibir llamadas
* 🔢 Capturar DTMF
* 🗄 Consultar base de datos
* 🌐 Consumir APIs externas
* 🔊 Responder en tiempo real al usuario
* 🧾 Registrar eventos en BD

---

# 🏗 Arquitectura General

```
Softphone (Zoiper/Eyebeam)
        │
        ▼
   Asterisk (PJSIP)
        │
        ▼
   Dialplan (extensions.conf)
        │
        ▼
      AMI
        │
        ▼
 Spring Boot (Listener)
        │
        ├── MySQL / PostgreSQL
        └── API externa
```

---

# ⚙️ Tecnologías Utilizadas

* Java 21+
* Spring Boot
* Asterisk-Java
* Asterisk 20+
* MySQL / PostgreSQL
* PJSIP
* VirtualBox (entorno de pruebas)

---

# 📁 Estructura del Proyecto

```
backend/
 ├── config/
 │     └── AmiConfig.java
 ├── service/
 │     ├── AmiListener.java
 │     ├── ClientService.java
 │     └── IvrService.java
 ├── controller/
 │     └── ClientController.java
 ├── repository/
 │     └── ClientRepository.java
 └── model/
       └── Client.java
```

---

# 📞 Flujo de Llamada Implementado

## 1️⃣ Entra llamada

```asterisk
[incoming]
exten => 100,1,Answer()
 same => n,Playback(menu)
 same => n,Read(OPCION,,1,,5)
 same => n,Set(__DTMF=${OPCION})
 same => n,NoOp(Opcion seleccionada es: ${OPCION})
 same => n,Wait(10)
 same => n,Hangup()
```

---

## 2️⃣ Spring captura DTMF vía AMI

Eventos capturados:

* DtmfBeginEvent
* DtmfEndEvent
* VarSetEvent
* NewExtenEvent
* HangupEvent

---

## 3️⃣ Validación en Base de Datos

```java
Client client = clientRepository
        .findByPhone(phone)
        .orElseThrow(() -> new RuntimeException("Client not found"));
```

---

## 4️⃣ Envío de variable a Asterisk

```java
SetVarAction action = new SetVarAction();
action.setChannel(channel);
action.setVariable("BALANCE");
action.setValue(client.getBalance().toString());
connection.sendAction(action);
```

---

# 🧠 Problemas Resueltos Durante el Desarrollo

## ✅ 1. Problema NAT / DTMF no llegaba

Solución en `pjsip.conf`:

```ini
dtmf_mode=rfc4733
rtp_symmetric=yes
force_rport=yes
rewrite_contact=yes
```

---

## ✅ 2. Timeout en SetVar (AMI Deadlock)

Problema:

```
Timeout waiting for response to SetVar
```

Causa: Se estaba usando `sendAction()` dentro del mismo thread del listener.

Solución:

```java
private final ExecutorService executor =
        Executors.newFixedThreadPool(5);

executor.submit(() -> procesarDTMF(event));
```

---

## ✅ 3. Recuperación correcta del caller

Se utiliza:

```java
Map<String, String> callSessions = new ConcurrentHashMap<>();
```

Indexado por `uniqueId` para mantener sesión por llamada.

---

# 🗄 Modelo de Cliente

```java
@Entity
public class Client {

    @Id
    private Long id;

    private String name;
    private String dni;
    private String email;
    private String phone;
    private String address;
    private String city;
    private Double balance;
    private Boolean isActive;
}
```

---

# 🔊 Ruta de Audios en Asterisk

Ubicación por defecto:

```
/var/lib/asterisk/sounds/
```

Ejemplo:

```
/var/lib/asterisk/sounds/menu.wav
```

---

# 🔧 Configuración AMI

## manager.conf

```ini
[general]
enabled = yes
port = 5038
bindaddr = 0.0.0.0

[admin]
secret = password
read = all
write = all
```

---

# 🧪 Pruebas Realizadas

* ✔ Registro SIP correcto
* ✔ DTMF recibido correctamente
* ✔ Cliente encontrado en BD
* ✔ Variable enviada a Asterisk
* ✔ Hangup normal
* ✔ Arquitectura multithread estable

---

# 🚀 Próximos Pasos

* 🔊 Generación dinámica de audio (TTS)
* 🌐 Integración con API externa real
* 📊 Registro de llamadas en tabla `call_log`
* 🔐 Seguridad AMI (IP whitelist)
* 🧵 Manejo avanzado de sesiones
* 🏗 Evaluar migración a ARI para mayor control

---

# 📌 Estado Actual del Proyecto

✔ IVR funcional ✔ Integración Asterisk–Spring estable ✔ DTMF bidireccional ✔ Consulta BD en tiempo real ✔ Arquitectura lista para entorno productivo básico

---


# 📦 Clients API – Spring Boot (Backend Enterprise Layer)

Como parte fundamental del IVR Banking System, desarrollamos un **microservicio backend independiente** encargado de la gestión, validación y consulta de clientes.

Este servicio es el núcleo de la lógica de negocio que permite:

* ✅ Validar clientes por ID / número de cuenta
* ✅ Consultar saldos en tiempo real
* ✅ Registrar resultados de validación
* ✅ Exponer endpoints REST para integración con IVR (Asterisk + AMI)
* ✅ Persistir auditoría en MySQL

---

## 🏗 Estructura del Proyecto

```
SpringBoot-Clients
│
├── src/main/java/com/backend/clients
│   ├── config          # Configuración general (CORS, Beans, Seguridad futura)
│   ├── controller      # Endpoints REST
│   ├── dto             # Objetos de transferencia de datos
│   ├── entity          # Entidades JPA (Clientes, Logs, etc.)
│   ├── exception       # Manejo global de excepciones
│   ├── repository      # Interfaces JPA Repository
│   ├── service         # Contratos de negocio
│   └── service/imp     # Implementación de lógica empresarial
│
├── src/main/resources
│   ├── application.yml / properties
│   ├── static
│   └── templates
│
└── src/test            # Pruebas unitarias
```

---

## 🔗 Integración con el IVR

El flujo completo funciona así:

1️⃣ Cliente llama al IVR (Asterisk)
2️⃣ Se captura DTMF
3️⃣ Evento enviado vía AMI al backend
4️⃣ Spring Boot consulta la base de datos MySQL
5️⃣ Se valida cliente y saldo
6️⃣ Se registra log de validación
7️⃣ Se devuelve respuesta para reproducción en IVR

---

## 🧠 Arquitectura Backend

* Arquitectura en capas (Controller → Service → Repository)
* Separación clara de responsabilidades
* Manejo centralizado de excepciones
* Preparado para escalabilidad y microservicios
* Compatible con futuras integraciones (Core Banking, APIs externas)

---

## 🗄 Base de Datos

Motor: **MySQL**
Funciones principales:

* Persistencia de clientes
* Registro de llamadas
* Registro de validaciones
* Auditoría de consultas

---

## 🚀 Preparado para Enterprise

* Diseño modular
* Código desacoplado
* Fácil integración con sistemas externos
* Base sólida para contenerización (Docker)
* Listo para despliegue en entornos cloud

---

Este módulo representa la capa inteligente del sistema IVR, donde ocurre toda la validación crítica y la lógica de negocio empresarial.

------

## 📄 Licencia

Este proyecto puede ser distribuido bajo licencia **MIT** (si corresponde). Agrega un archivo `LICENSE` en la raíz si deseas publicarlo.

---

## 📬 Contacto

Para dudas, sugerencias o contribuciones:

📧 **[casseli.layza@gmail.com](mailto:casseli.layza@gmail.com)**

🔗 [LinkedIn](https://www.linkedin.com/in/casseli-layza/)
🔗 [GitHub](https://github.com/CasseliLayza)

💡 **Desarrollado por Casseli Layza como parte de un proyecto con Plataformas Contact Centers & Spring Boot.**

***💚 ¡Gracias por revisar este proyecto!... Powered by Casse 🌟📚🚀...!!***

## Derechos Reservados

```markdown
© 2026 Casse. Todos los derechos reservados.
```
