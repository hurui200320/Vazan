# Vazan

This is a storage management app that fit my own needs. This is also my (officially) first android
app. I tried to develop android app multiple times before, but those eventually ended up into trash
bins, so those don't count.

## Why I need this?

Several times a year, I need to pack all my stuff and move. After moving to a new place, I don't
unpack all things immediately. When I need to find exactly one specific object, it would be a
nightmare to go through all boxes: unpack, dump out all things, not found, repack.

One day I was picking up a package, I saw the waybill on the box. I thought, why don't I print
something and stick it to the box, so I can find out what inside without actually open it? So I
wrote this app.

## What it does?

To "print" something, I mean print something using a thermal printer. It requires certain types of
paper to heat/print, and the heated part will become black. It can't print color, but good enough
for printing barcodes and text.

I use Paperang P2, a 300dpi printer using 57mm wide thermal paper (576 dot per line). I will use
thermal label sticker so I can stick it to the box without any additional tape and glue. However,
this is not a open product. Actually, it's illegal to reverse engineer the software, but thanks to
github and google, I found some datasheet that at least working from my side.

The printer part is somehow awkward: Commercial 2C products are not open, they try to lock you into
their app and environment, but it does have some good features, like high print DPI, portable (with
battery). Commercial 2B products are open, they will give you their SDKs and glad to see you
supporting their devices, but most of them are big and rely on wall power. **If there are any
portable opensource bluetooth thermal printer, I'm supper glad to know and support it.**

This app use bluetooth to communicate with the printer, aka RFCOMM,
see [hurui200320/java-paperang-p2-usb](https://github.com/hurui200320/java-paperang-p2-usb)
for more info about the protocol.

There are 4 types of barcode: QR code, Aztec, PDF417 and Data Matrix, they all have some certain of
error correction to resist damage. Also, it will print the human readable text below the barcode. If
the code is unreadable, you can manually type the text and find related info.

For storage, I use local SQLite database. Each box are assigned with a UUID. The content of the
barcode is the string value of a uuid. For each UUID, you can have multiple immutable notes, with
creation time recorded, so you can know what is put inside and when was took out.

To record a new box, first print some barcode and stick it to the box. Then scan the barcode, or
type in UUID manually, then you can add notes. When you need to find something, just search the item
name and it will find all UUIDs that contains related notes. And you can find the box based on the
printed label/sticker, which has a human readable uuid on it.

This app also came with a backup feature. You can export all notes into a JSON file, and import it
later.

## Used tech

+ [Compose](https://developer.android.com/jetpack/compose) for UI (I hate XML)
+ [Room](https://developer.android.com/training/data-storage/room) for SQLite
+ [Material Design](https://m2.material.io/develop/android) for a good out-of-box visual look
+ [camerax](https://developer.android.com/training/camerax)
  and [ml kit](https://developers.google.com/ml-kit/vision/barcode-scanning) for scanning and
  decoding barcode
+ [zxing](https://github.com/zxing/zxing) for generating barcodes
+ [Kotlin](https://kotlinlang.org) for programming

Thanks all related developers for sharing the protocol details for paperang P2.
