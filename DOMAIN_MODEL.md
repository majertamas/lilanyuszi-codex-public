# Lilanyuszi – Domain Model

## Overview

Lilanyuszi is a collaborative application where users can create and share different kinds of lists.

The core concept of the application is **Shared Access**.

Every list has its own members, owner, permissions and user-specific aliases.

There is **no global group** concept.

Each list is shared independently.

---

# Core Concepts

## User

A user represents an authenticated person.

A user may:

* own multiple lists
* participate in multiple lists owned by other users
* define a personal alias for every shared list

---

## Shared Access

`SharedAccess` is the ownership and permission boundary of the application.

Every shareable resource owns exactly one `SharedAccess`.

Responsibilities:

* canonical list name
* owner
* members
* aliases
* list type
* uniqueness

Current supported type:

* SHOPPING

Future types:

* TODO
* RECIPE

---

## Ownership

Every SharedAccess has exactly one owner.

The owner is stored in:

```
SharedAccess.ownerUser
```

Ownership is never stored anywhere else.

The owner is automatically considered a member of the list.

There can never be multiple owners.

---

## Membership

Membership only answers one question:

> Does this user have access to this list?

`SharedAccessMember` contains no permissions or roles.

It simply represents access.

The owner's membership is stored exactly like every other member.

---

## Role

Role is derived.

If

```
sharedAccess.ownerUser == currentUser
```

then the role is:

```
OWNER
```

otherwise:

```
MEMBER
```

No role is persisted in the database.

---

## Aliases

Every member may define a personal alias for a shared list.

Example:

Canonical name:

```
Family Shopping
```

Displayed names:

```
Alice:
Groceries

Bob:
Weekend Shopping

Charlie:
Family
```

Aliases are user-specific.

Aliases never affect canonical uniqueness.

---

# Canonical Name

Every list stores:

* name
* canonicalName

Example:

```
Name:
Shopping

Canonical:
shopping
```

The canonical name is used for comparisons and uniqueness.

The displayed name is always the original name.

---

# Uniqueness

A user cannot own two lists with the same:

* owner
* type
* canonical name

Current uniqueness rule:

```
(ownerUser, type, canonicalName)
```

Different owners may use identical names.

Aliases are ignored.

---

# Shopping List

A shopping list is the first concrete implementation of SharedAccess.

```
SharedAccess
        │
        ▼
ShoppingList
        │
        ▼
ShoppingItem
```

ShoppingList does not duplicate ownership or permissions.

All permission information comes from SharedAccess.

---

# Shopping Items

Shopping items belong to exactly one ShoppingList.

A shopping item cannot exist without a shopping list.

Deleting a shopping list deletes all shopping items.

---

# Deletion

Deleting a list removes:

* SharedAccess
* ShoppingList
* ShoppingItems
* SharedAccessMembers
* SharedAccessAliases

No orphaned data should remain.

---

# Leaving a List

## Member

When a normal member leaves:

* membership is removed
* alias is removed

The list continues to exist.

---

## Owner

When the owner leaves:

the entire list is deleted.

Ownership is never transferred automatically.

---

# Security Rules

A user may access a list only if they are a member.

Only the owner may:

* delete the list
* manage members
* perform owner-only operations

---

# Aggregate

The aggregate root is:

```
SharedAccess
```

Everything related to ownership, permissions and collaboration belongs to this aggregate.

Concrete list implementations (Shopping, Todo, Recipe, ...) contain only domain-specific data.

---

# Future Extensions

The current model is intentionally designed so that additional list types can be added without changing the sharing model.

Examples:

* TodoList
* RecipeCollection
* Wishlist
* Inventory
* Notes

Every future shareable resource should reuse the existing SharedAccess infrastructure.

---

# Domain Principles

* One owner per shared resource.
* Membership only grants access.
* Ownership is stored exactly once.
* Aliases are user-specific.
* Canonical names provide uniqueness.
* Business rules belong to SharedAccess.
* Concrete list implementations contain only domain-specific data.
