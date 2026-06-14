-- KEYS[1]=session hash, KEYS[2]=user sessions set, KEYS[3]=global zset
-- ARGV[1]=jti
redis.call('DEL', KEYS[1])
redis.call('SREM', KEYS[2], ARGV[1])
redis.call('ZREM', KEYS[3], ARGV[1])
return 1
