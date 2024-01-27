# Vazan

**_Note: This branch is V3, a half rewrite which switched from memento database to my own backend.
For V2, see `memento-archive`.
For the v1, see branch `old-archive`._**

This is a supplementary app for [jim-cli](https://github.com/hurui200320/jim-cli) that fit my own needs
for personal inventory management.

## Why rewrite again?

Considering I'm still unemployed, which means I have no incoming but still need to spend money to survive,
as a action to reduce my daily spending (thus to increase the chance I live), I rolled out the memento
database which charges 80USD per year instead 60USD after adding in their AI thing, which I have no use for.

Thus I wrote [jim-cli](https://github.com/hurui200320/jim-cli), a cli-based inventory management program.
The cli interface is good enough for most times, but I still need a way to print barcode and work without
laptop in reach, so I designed a HTTP interface and adopted this app from memento database to jim.

## What it does?

It connects to [jim-cli](https://github.com/hurui200320/jim-cli) using HTTP but in an encrypted way.
Functionally speaking, it can do everything the cli program can do: CRUD of entries and metadata,
search for keywords, etc.
Additionally, it has some support for barcode scanning and printing.

## Barcode

Barcode is the core of this app. In jim-cli, there is no requirement about what id you use, since it's just a cli program.
This app however, designed a label encoding system for boxes and items,
where you can actually print it using thermal printers and stick them on the side of boxes and items.

The barcode is 10 digits long, the first letter marks the type: `B` for boxes, `I` for items.
The rest 9 digits encodes a number using characters from `23456789ABCDEFGHJKLMNPQRSTUVWXYZ`.
Notice that there is no `1` and `L`, `0` and `o`, such characters and cause confusion when manually typing.
In total, 9 digits can represent about 35 trillion entries.
Considering we randomly generate the label, unless you really have a lot of things to store,
the collision chance is fairly small.
But I do have procedures to prevent collision.

The encoding for each digits is different too, so a smaller number like 2 won't looks like `AAAAAAAA2`,
but instead something like `ZJRBAPUEM`.
This is ensure to not generate similar looking labels that will cause confusion when someone trying to
spot some box with certain barcode but ended up picking up the wrong one with 1 digit different.

Currently, I choose CODE 128 for 1D barcode and DATA MATRIX for 2D barcode.
I recommend using DATA MATRIX for box which often offers a large surface area.
In my region, post office use CODE 128 and QrCode on their tickets, so I choose DATA MATRIX to avoid
accidentally scan the ticket barcode instead of my own one.

For barcode scanning, I use [Google's barcode scanner](https://developers.google.com/ml-kit/vision/barcode-scanning),
so you can fork and modify to fit your own barcode choice.

## Thermal printer

My thermal printer is G-Printer GP-M322.
It support multiple modes but I use TSPL command set for sticker printing.
It uses bluetooth SPP, which you need to pair with your phone, then the app make a socket connection
to transfer printing command.
The SPP UUID is `00001101-0000-1000-8000-00805F9B34FB`.
If your printer use the same bluetooth SPP UUID and support TSPL command set, then the app should work with your printer.

To print a barcode, the app first generate a picture of the barcode, then print the raw data using TSPL command.
No need to support different barcodes on the printer side.

Paper types are defined by the size: width, length and gap.
Currently all my papers are 2mm gap, and I only write support for 80mm x 60mm, 60mm x 80mm and 70mm x 30mm.
I found them works pretty well with my printer.
However, you can easily extend the paper types by generating the corresponding image and turns it into TSPL commands. 

The general layouts are as follow.

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

For paper shaped like a square, 2D barcode is used, along with the text version of the code, in case
the barcode is damaged.

Code 128, which is a slim barcode, is used on a slim paper type:

```
 ┌─────────────────────────────────────────────────────────────┐
 │      ┌───────────────────────────────────────────────┐      │
 │      │                 CODE128 BARCODE               │      │
 │      └───────────────────────────────────────────────┘      │
 │                 The human readable label                    │
 └─────────────────────────────────────────────────────────────┘
```

## Quick operations

Beside normal CRUD of data, with the power of scanning a barcode, we can have some quick operations.

### Quick Add

After printing a bunch of stickers, it would be great to scan them and add them into the system,
before you stick them on the box or something else.

This is a procedure to ensure your barcode is fresh and new. If you trying to add an existing barcode,
the app will notice you.

The newly added entries will be placed under root.

### Quick move

Want to move a lot of boxes or items to a new location, but don't want to find and update one by one?

Just type (location id) or scan (box id) the target entry id, then keep scanning the item/box you move in.
You can update up to 10 boxes/items per minute.
Much faster than updating one by one.

### Quick view

In jim-cli, you have to type the entry id letters by letters if you want to view the details of it.
With the barcode scanner, just scan the barcode and the app will show you the detail.

No more typing.

## Can anyone else use this app?

Sure. Despite this app is solely created to meet my own need, if you have similar need, and you're
willing to use jim-cli, then you can use this app freely.

But do notice that this app comes with no warranty. You use it on your own.

I may or may not help you solve your questions, implement new features you want/ask.
No promise for now.

But you're welcome to make modify to this app: fork it and change whatever fit your need.
If you're generous enough, make a pr. If I don't like your pr, you may become a separate
fork which may ended up better than my implementation. That's why I choose opensource.
