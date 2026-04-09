# Data Flow Diagram (DFD)

## Flow

1. HH triggers import (manual or automated)
2. HH reads AK content provider
3. HH receives JSON payload
4. Parser converts JSON → DTO
5. Mapper converts DTO → Domain Models
6. Mapper converts Domain → Entities
7. Repository replaces snapshot (transaction)
8. Timeline reads entities
9. UI renders imported meals/logs

## Key Rule
Always replace entire day snapshot — never partial merge.
