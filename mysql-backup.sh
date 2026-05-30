mysqldump \
  -h 192.168.3.4\
  -P 3306 \
  -u root \
  -p \
  --default-character-set=utf8mb4 \
  --single-transaction \
  --routines \
  --triggers \
  --events \
  --hex-blob \
  --column-statistics=0 \
  --databases auth_admin_base \
  > "auth_admin_base_full_$(date +%Y%m%d_%H%M%S).sql"