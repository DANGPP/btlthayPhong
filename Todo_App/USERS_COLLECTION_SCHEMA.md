# Users Collection Schema

## Collection Setup in Appwrite

### Collection Information
- **Collection ID**: `users`
- **Collection Name**: `users`
- **Database ID**: `6908cde40006b4bbd549`

### Attributes

| Attribute Name | Type | Size | Required | Default | Array | Description |
|---------------|------|------|----------|---------|-------|-------------|
| `email` | string | 255 | ✅ Yes | - | No | User email address |
| `password` | string | 255 | ✅ Yes | - | No | Hashed password (BCrypt) |
| `name` | string | 100 | ✅ Yes | - | No | User display name |
| `avatar` | string | 500 | ❌ No | NULL | No | Avatar URL (optional) |
| `phone` | string | 20 | ❌ No | NULL | No | Phone number (optional) |
| `bio` | string | 500 | ❌ No | NULL | No | User bio (optional) |
| `$createdAt` | datetime | - | - | - | No | System field |
| `$updatedAt` | datetime | - | - | - | No | System field |

### Indexes

#### 1. Unique Index on email
- **Index Key**: `unique_email`
- **Type**: Unique
- **Attributes**: `email`
- **Orders**: ASC
- **Purpose**: Prevent duplicate emails, used for login

#### 2. Full-text Index on email
- **Index Key**: `search_email`
- **Type**: Fulltext
- **Attributes**: `email`
- **Purpose**: Enable email search

#### 3. Full-text Index on name
- **Index Key**: `search_name`
- **Type**: Fulltext
- **Attributes**: `name`
- **Purpose**: Enable name search

### Permissions

Set these permissions in Appwrite Console:

#### Read Access
- `Any` - All users can read user profiles

#### Create Access
- `Any` - Allow user registration (will be created via Auth flow)

#### Update Access
- `User:[USER_ID]` - Users can only update their own profile

#### Delete Access
- `User:[USER_ID]` - Users can only delete their own account

### Step-by-Step Setup

1. **Go to Appwrite Console**
   - Navigate to Databases → Select your database
   - Click "Create Collection"

2. **Create Collection**
   - Name: `users`
   - Collection ID: `users`
   - Click "Create"

3. **Add Attributes** (in order)
   ```
   1. email (string, 255, required)
   2. password (string, 255, required)
   3. name (string, 100, required)
   4. avatar (string, 500, optional)
   5. phone (string, 20, optional)
   6. bio (string, 500, optional)
   ```

4. **Create Indexes**
   - Click "Indexes" tab
   - Add each index as specified above

5. **Set Permissions**
   - Click "Settings" tab
   - Configure permissions as specified above

### Example Document

```json
{
  "$id": "unique_document_id",
  "authId": "692571e5001c11479354",
  "email": "user@example.com",
  "name": "John Doe",
  "avatar": null,
  "phone": null,
  "bio": null,
  "$createdAt": "2025-12-06T10:30:00.000Z",
  "$updatedAt": "2025-12-06T10:30:00.000Z"
}
```

## Migration Strategy

### For Existing Users (Already using Auth)

Run this migration after creating the collection:

```kotlin
// In your app, add a one-time migration function
suspend fun migrateAuthUsersToDatabase() {
    val currentUser = account.get()
    
    // Check if user exists in database
    val existingUser = try {
        databases.listDocuments(
            databaseId = DATABASE_ID,
            collectionId = "users",
            queries = listOf(Query.equal("authId", currentUser.id))
        ).documents.firstOrNull()
    } catch (e: Exception) {
        null
    }
    
    // Create user if doesn't exist
    if (existingUser == null) {
        databases.createDocument(
            databaseId = DATABASE_ID,
            collectionId = "users",
            documentId = ID.unique(),
            data = mapOf(
                "authId" to currentUser.id,
                "email" to currentUser.email,
                "name" to currentUser.name
            )
        )
    }
}
```

### For New Users

Users will be automatically created in the database during registration/login.

## Usage in Code

### Create User on Registration
```kotlin
// After Auth account creation
val user = account.create(ID.unique(), email, password, name)

// Create user in database
databases.createDocument(
    databaseId = DATABASE_ID,
    collectionId = "users",
    documentId = ID.unique(),
    data = mapOf(
        "authId" to user.id,
        "email" to email,
        "name" to name
    )
)
```

### Search Users
```kotlin
// Search by email
val users = databases.listDocuments(
    databaseId = DATABASE_ID,
    collectionId = "users",
    queries = listOf(Query.search("email", searchQuery))
)

// Search by name
val users = databases.listDocuments(
    databaseId = DATABASE_ID,
    collectionId = "users",
    queries = listOf(Query.search("name", searchQuery))
)
```

### Get All Users
```kotlin
val users = databases.listDocuments(
    databaseId = DATABASE_ID,
    collectionId = "users",
    queries = listOf(Query.limit(100))
)
```
