const { setGlobalOptions } = require("firebase-functions");
const { onValueCreated, onValueUpdated } = require("firebase-functions/v2/database");
const { initializeApp } = require("firebase-admin/app");
const { getDatabase } = require("firebase-admin/database");
const { getMessaging } = require("firebase-admin/messaging");

initializeApp();

// Kharcha control: ek saath zyada se zyada 10 copies chalengi
setGlobalOptions({ maxInstances: 10 });

// uid ke saare phones par notification bhejo, aur bekaar ho chuke tokens saaf kar do
async function sendToUid(uid, data) {
  if (!uid) return;
  const snap = await getDatabase().ref(`FcmTokens/${uid}`).get();
  if (!snap.exists()) return;

  const tokens = Object.keys(snap.val());
  const res = await getMessaging().sendEachForMulticast({
    tokens,
    data, // sirf data: app khud notification banati hai (AppMessagingService)
    android: { priority: "high" },
  });

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
  await Promise.all(dead.map((t) => getDatabase().ref(`FcmTokens/${uid}/${t}`).remove()));
}

// 1) User order kare -> us order ke har admin ko notification
//    (placeOrder har admin ke liye Orders/ByAdmin/<adminUid>/<orderId> likhta hai)
exports.notifyAdminOnNewOrder = onValueCreated(
  "/Orders/ByAdmin/{adminUid}/{orderId}",
  async (event) => {
    const order = event.data.val();
    if (!order) return;

    const items = Array.isArray(order.items) ? order.items : Object.values(order.items || {});
    const pieces = items.reduce((n, i) => n + (i.quantity || 0), 0);

    await sendToUid(event.params.adminUid, {
      title: "New order received",
      body: `${pieces} ${pieces === 1 ? "item" : "items"} • ₹${order.totalPrice}`,
      orderId: String(event.params.orderId),
    });
  }
);

// 2) Admin status badle -> us order ke user ko notification
exports.notifyUserOnStatusChange = onValueUpdated(
  "/Orders/ByAdmin/{adminUid}/{orderId}",
  async (event) => {
    const before = event.data.before.val();
    const after = event.data.after.val();
    if (!before || !after || before.status === after.status) return;

    await sendToUid(after.userUid, {
      title: "Order update",
      body: `Your order is now ${after.status}`,
      orderId: String(event.params.orderId),
    });
  }
);