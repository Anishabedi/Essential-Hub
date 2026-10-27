// Laptop par chalane wali script: node listener.js
// Kaam wahi jo index.js (Cloud Functions) karta hai, bas Firebase ke server ki jagah tumhare computer par.
//   1) Naya order aaye  -> us order ke admin ko notification
//   2) Status badle     -> us order ke user ko notification
//
// IMPORTANT: serviceAccountKey.json kabhi GitHub / Drive / kisi ko mat bhejna.
// IMPORTANT: ye script sirf EK computer par chale, warna har notification do baar jayega.

const { initializeApp, cert } = require("firebase-admin/app");
const { getDatabase } = require("firebase-admin/database");
const { getMessaging } = require("firebase-admin/messaging");

// Firebase console > Project settings > Service accounts > Generate new private key
// Download hui file ka naam serviceAccountKey.json rakho aur isi folder mein daalo.
const serviceAccount = require("./serviceAccountKey.json");

initializeApp({
  credential: cert(serviceAccount),
  // Firebase console > Realtime Database > Data tab ke upar jo URL likha hai wahi yahan daalo.
  // Jaise: https://<project-id>-default-rtdb.firebaseio.com
  // ya:    https://<project-id>-default-rtdb.asia-southeast1.firebasedatabase.app
  databaseURL: "https://essential-hub-992dd-default-rtdb.firebaseio.com/",
});

const db = getDatabase();

// uid ke saare phones par notification bhejo, aur bekaar ho chuke tokens saaf kar do
async function sendToUid(uid, data) {
  if (!uid) return;
  const snap = await db.ref(`FcmTokens/${uid}`).get();
  if (!snap.exists()) {
    console.log(`Token nahi mila: ${uid}`);
    return;
  }

  const tokens = Object.keys(snap.val());
  const res = await getMessaging().sendEachForMulticast({
    tokens,
    data, // sirf data: app khud notification banati hai (AppMessagingService)
    android: { priority: "high" },
  });
  console.log(`Bheja -> ${uid}: ${res.successCount} safal, ${res.failureCount} fail`);

  const dead = [];
  res.responses.forEach((r, i) => {
    const code = r.error && r.error.code;
    if (
      !r.success &&
      (code === "messaging/registration-token-not-registered" ||
        code === "messaging/invalid-registration-token")
    ) {
      dead.push(tokens[i]);
    }
  });
  await Promise.all(dead.map((t) => db.ref(`FcmTokens/${uid}/${t}`).remove()));
}

function countPieces(order) {
  const items = Array.isArray(order.items) ? order.items : Object.values(order.items || {});
  return items.reduce((n, i) => n + (i.quantity || 0), 0);
}

// "adminUid/orderId" -> status. Pehli baar null: purane orders ke notification na jayein.
let known = null;

db.ref("Orders/ByAdmin").on(
  "value",
  async (snap) => {
    const data = snap.val() || {};

    const current = new Map();
    for (const adminUid of Object.keys(data)) {
      const orders = data[adminUid] || {};
      for (const orderId of Object.keys(orders)) {
        current.set(`${adminUid}/${orderId}`, { adminUid, orderId, order: orders[orderId] });
      }
    }

    // Script shuru hote hi jo orders pehle se hain, unhe bas yaad rakh lo
    if (known === null) {
      known = new Map([...current].map(([k, v]) => [k, v.order.status]));
      console.log(`Shuru: ${known.size} purane orders yaad kiye, ab naye ka intezaar...`);
      return;
    }

    const jobs = [];
    for (const [key, { adminUid, orderId, order }] of current) {
      if (!known.has(key)) {
        // Naya order -> admin ko
        const pieces = countPieces(order);
        console.log(`Naya order: ${key}`);
        jobs.push(
          sendToUid(adminUid, {
            title: "New order received",
            body: `${pieces} ${pieces === 1 ? "item" : "items"} • ₹${order.totalPrice}`,
            orderId,
          })
        );
      } else if (known.get(key) !== order.status) {
        // Status badla -> user ko
        console.log(`Status badla: ${key} -> ${order.status}`);
        jobs.push(
          sendToUid(order.userUid, {
            title: "Order update",
            body: `Your order is now ${order.status}`,
            orderId,
          })
        );
      }
    }

    known = new Map([...current].map(([k, v]) => [k, v.order.status]));

    try {
      await Promise.all(jobs);
    } catch (e) {
      console.error("Notification bhejne mein error:", e);
    }
  },
  (err) => console.error("Database listener error:", err)
);

process.on("unhandledRejection", (e) => console.error("Unhandled:", e));
console.log("Listener chalu hai. Band karne ke liye Ctrl+C dabao.");