# Rebuild after code changes
docker compose up --build

# View app logs only
docker compose logs -f app

# Restart app only
docker compose restart app