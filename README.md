# Vazan

**_Note: This branch is V2, a completely rewrite. For the v1, see branch `old-archive`._**

This is a supplementary app for [Memento Database](https://mementodatabase.com/) that fit my own needs
(for personal inventory management).

Also, this is my (officially) first android app. I tried to develop android app multiple times before,
but those eventually ended up into trash bins, so they don't count.

## Why I need this?

Several times a year, I need to pack all my stuff and move. After moving to a new place, I don't
unpack all things immediately. When I need to find exactly one specific object, it would be a
nightmare to go through all boxes: unpack, dump out all things, not found the thing, repack the mess.

One day I was picking up a package, I saw the waybill on the box. I thought, why don't I print
something and stick it to the box, so I can find out what inside without actually open it? So I
wrote this app.

## What it does?

First of all, in V2, all data is stored in the memento database, an Android/iOS app that offers
something like a database, but not technically the relational database. However, it support barcode
and foreign key (the ability to reference other entities in a library/table), and is pretty good to
use.

> Note: Memento database does require a subscription to unlock the full feature (cloud thing), and
> this app uses the cloud api they offer. So technically speaking, there is a paywall.

Despite Memento database offers a great way to store data and to interact with them (the UI part is
the most important part, I could never make something like that, frontend is just too hard to me),
it doesn't know how to print labels.

By design, you print label stickers (normally using thermal printer) and stick them to your containers
and items, then use memento database to link those stickers (barcode) to the box/item. Now you can
find all your items on your phone. To print those stickers, you have to make sure they are unique.
Otherwise you will have two box with same barcode. But how?

The simplest solution is to use the memento database's export function. Export your existing barcodes
into a CSV file, and somehow make sure you don't print the existing label again. But that requires
software support. A lot of software can print data from a excel, but can't generate data to avoid a
given excel.

Yeah, I know, you can write a simple Java/Python/Shell script to do that. But do you really want to
use your laptop when you're moving? It's already a mess, and you add more. However, using a phone will
not contribute to the mess. So I wrote this app.

By using the memento database's cloud api, this app can "sync", aka read your existing labels (box
and item), store in it local cache. When generating new label, the app will avoid those existing ones,
and ensure each time new one will be printed. Also, the app will track which label is printed, so you
don't accidentally print two identical labels in different time.

Besides, the app support "quick scan". For example, you're moving 10 box to a new location. To apply
that change, you have to search the box and change the location, which requires multiple click on screen.
The quick scan let you select one location, then scan multiple labels, send the change request to cloud.
No need to click on screen anymore, just scan it.

## What about print?

The print part makes this app not easy to use. Since the printer driver is hardcoded.

My thermal printer is G-Printer GP-M322. This is not the best thermal printer (can't print decent 
pictures), but it does print labels, and print them really well. And most important, is that G-Printer
offers SDK. Although the SDK is old and not-well-organized, the protocol is open. I implement my
own driver for this printer, and it works well. In v1 I use paperang P2, which has no public documents,
not mention SDKs.

About labels, I use two different barcodes. For box, you got a lot of space to stick, thus I use 
Data Matrix with a vertical layout:

```
 ┌───────────────────────────────┐
 │                               │
 │      ┌─────────────────┐      │
 │      │                 │      │
 │      │   DATA MATRIX   │      │
 │      │     BARCODE     │      │
 │      │                 │      │
 │      └─────────────────┘      │
 │                               │
 │   The human readable label    │
 │                               │
 └───────────────────────────────┘
```

The data matrix offers a decent error correction, but in case the barcode is not working, there are
also the text label for human to read. The encoding is special: it use digits and uppercase letter,
but without digits 0 and 1, and letter o and i, thus you won't have to guess if a character is O or 0,
1 or I.

For item, I use code128, which is a slim barcode, which considered the limited space you have on a item:

```
 ┌─────────────────────────────────────────────────────────────┐
 │      ┌───────────────────────────────────────────────┐      │
 │      │                 CODE128 BARCODE               │      │
 │      └───────────────────────────────────────────────┘      │
 │                 The human readable label                    │
 └─────────────────────────────────────────────────────────────┘
```

## What about memento's cloud API?

To use their api, you firstly need an API key, which can be created on their desktop software.
The API key must have read and write permission, and the libraries can only contains your 3 essential
libraries.

There are 3 libraries required: Location, Box and item.

The Location must has a text field for the location, like "XX, YY str., ZZ city", so you can distinguish
it in the app.

The Box must has a barcode field for label, you may set it as read-only so you won't accidentally changed it.
Also it requires a parent location field liked to location, and a parent box field like to box. The box
can be located in one location, or in other boxes.

The item is same, a barcode field for label, a parent location field liked to location, and a parent
box field like to box.

The barcode field is not required (you can have unlabeled box and items), but this app requires a
label/barcode to index the item.

Also, the API has rate limit. For personal plan, the limit is 30 request per minutes, thus I have some
hardcoded delay in the code to ease the rate limit.

## What about my data?

No data will be uploaded to me. It only send request to memento's cloud using your API key.

To backup your local database (the one for app storing labels, settings), you can export them
as a bencode file, and import it later.

## But how can I use it?

As I said, this is an app for my own need, so there is no public build to install. However, if this
app _accidentally_ fit your needs too, and you _accidentally_ want to use this app, with **no guaranteed
support or updates from me**, then feel free to open an issue, or send me an email, I'm glad to help.
