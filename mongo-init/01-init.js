db = db.getSiblingDB('activitydb');
db.createUser({
  user: process.env.MONGO_ACTIVITY_USER || 'activityuser',
  pwd: process.env.MONGO_ACTIVITY_PASSWORD || 'activitypass',
  roles: [{ role: 'readWrite', db: 'activitydb' }]
});