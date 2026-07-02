# DbGate Configuration - Audit Service

This directory contains the DbGate database administration tool configuration for the Audit Service local development environment.

## Pre-configured Connections

The `connections.jsonl` file contains pre-configured connections for all Audit service infrastructure:

### 1. PostgreSQL - Audit Service

- **ID**: `postgres-audit`
- **Server**: `postgres-auditservice:5432`
- **Database**: `audit`
- **User**: `svc_audit_dba`
- **Engine**: `postgres@dbgate-plugin-postgres`

### 2. RabbitMQ - Audit Service

- **ID**: `rabbitmq-management`
- **Server**: `rabbitmq-auditservice:15672`
- **User**: `svc_audit_rmq`
- **Engine**: `rabbitmq@dbgate-plugin-rabbitmq`

## Usage

### Start Infrastructure with DbGate

```bash
# Start infrastructure only (for IDE development)
docker compose up -d

# Access DbGate at: http://localhost:3100
```

**Note**: DbGate runs on port **3100** (not 3000) to avoid conflict with Grafana.

### Start Full Stack with DbGate

```bash
# Start infrastructure + Audit service
docker compose -f compose.container.yaml up -d

# Access DbGate at: http://localhost:3100
```

### Access DbGate UI

Once the services are running, open your browser to:

**http://localhost:3100**

All connections are pre-configured and ready to use.

## What You Can Do with DbGate

### PostgreSQL Database - Audit Events

The audit service uses PostgreSQL to store:

- Audit event records (security events, data changes, admin actions)
- User activity logs
- System-wide audit trails
- Compliance and forensic data

With DbGate you can:

- Browse audit event tables and schemas
- Execute SQL queries to search audit logs
- Export audit data (CSV, JSON, SQL) for compliance reports
- Analyze audit patterns with SQL aggregations
- View table structure and indexes

### RabbitMQ - Event Consumption

The audit service consumes events from RabbitMQ:

- User events (user.created, user.deleted, etc.)
- Tenant events (tenant.created, tenant.suspended, etc.)
- Subscription events (subscription.created, subscription.cancelled, etc.)
- Custom audit events from other services

With DbGate you can:

- View queues and their message counts
- Monitor exchange bindings
- Check connection statistics
- View queue configurations
- Monitor message consumption rates

## Audit Service Specifics

The Audit Service is a **passive observer** in the platform:

- Consumes events from the `iqkv.events` topic exchange
- Stores normalized audit records in PostgreSQL with JSONB metadata
- Provides query API for audit trail searches
- Does not publish events (write-only from perspective of event bus)

### Typical Queries

Browse recent audit events:

```sql
SELECT * FROM audit_records
ORDER BY created_at DESC
LIMIT 100;
```

Find events by user:

```sql
SELECT * FROM audit_records
WHERE user_id = 'user-uuid'
ORDER BY created_at DESC;
```

Find events by tenant:

```sql
SELECT * FROM audit_records
WHERE tenant_id = 'tenant-uuid'
ORDER BY created_at DESC;
```

Search by event type:

```sql
SELECT * FROM audit_records
WHERE event_type = 'user.created'
ORDER BY created_at DESC;
```

Aggregate events by type:

```sql
SELECT event_type, COUNT(*)
FROM audit_records
GROUP BY event_type
ORDER BY COUNT(*) DESC;
```

## Updating Credentials

If you change credentials in your `.env` or compose files, update `connections.jsonl`:

```json
{
    "_id": "postgres-audit",
    "engine": "postgres@dbgate-plugin-postgres",
    "server": "postgres-auditservice",
    "port": 5432,
    "user": "NEW_USER",
    "password": "NEW_PASSWORD",
    "database": "auditservice",
    "displayName": "PostgreSQL - Audit Service"
}
```

Then restart DbGate:

```bash
docker compose restart dbgate
```

## Troubleshooting

### DbGate won't start

```bash
# Check logs
docker logs foundation-auditservice-dbgate-dev

# Verify connections file exists
ls docker/dbgate/connections.jsonl

# Restart DbGate
docker compose restart dbgate
```

### Can't connect to PostgreSQL

```bash
# Verify PostgreSQL is running and healthy
docker ps --filter name=foundation-auditservice-postgres-dev

# Check PostgreSQL logs
docker logs foundation-auditservice-postgres-dev

# Test connection from DbGate container
docker exec foundation-auditservice-dbgate-dev ping postgres-auditservice
```

### Can't connect to RabbitMQ

```bash
# Verify RabbitMQ is running
docker ps --filter name=foundation-auditservice-rabbitmq-dev

# Check RabbitMQ logs
docker logs foundation-auditservice-rabbitmq-dev

# Ensure management plugin is enabled
docker exec foundation-auditservice-rabbitmq-dev rabbitmq-plugins list
```

### Reset DbGate Data

```bash
# Stop and remove DbGate
docker compose stop dbgate
docker compose rm -f dbgate

# Remove volume
docker volume rm iqkv_auditservice_dbgate_data_dev

# Restart
docker compose up -d dbgate
```

## Port Configuration

DbGate runs on port **3100** to avoid conflict with Grafana (port 3000):

```yaml
ports:
    - "3100:3000" # DbGate Web UI
```

## Security Notes

⚠️ **Important**: This configuration is for local development only.

- Credentials are stored in plaintext in `connections.jsonl`
- Do not commit real production credentials to version control
- DbGate port 3100 is exposed to localhost only
- Authentication is enabled (`LOGINS=1`)
- Audit data may contain sensitive information - protect accordingly

## Compose File Configuration

DbGate is included in:

- ✅ `compose.yaml` - Infrastructure only (use with IDE)
- ✅ `compose.container.yaml` - Full stack (infrastructure + service)
- ✅ `compose.base.yaml` - Base definitions

All three configurations include DbGate by default.

## More Information

- [DbGate Official Documentation](https://dbgate.org/docs/)
- [DbGate GitHub Repository](https://github.com/dbgate/dbgate)
- [Supported Database Engines](https://dbgate.org/docs/databases.html)

## Tips for Audit Service

1. **Query History**: DbGate saves your audit query history for forensic analysis
2. **Export Compliance Reports**: Export audit data as CSV for compliance documentation
3. **JSONB Queries**: Use PostgreSQL JSONB operators to query event metadata
4. **Time Range Filters**: Use `created_at` with date ranges for time-based analysis
5. **Event Aggregation**: Use GROUP BY to analyze audit patterns and anomalies
6. **Dark Mode**: Easier viewing for long audit review sessions
