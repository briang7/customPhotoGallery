# customPhotoGallery

Sample code from NatureFinder Android App.  This pulls all of the photos from the users phone and displays the thumbnails in a recyclerview grid at the bottom of the screen.  When selected a preview will show at the top 2/3rds of the screen with zoom capabilities.  The limit is set to 5 selected photos.  

Some phones will rotate the pictures so I had to reorient each picture so that it is faceing right side up.  This takes a little bit of time, so it is done in the background.  Also put a check in place, using the recyclerview's hexcode, to make sure that the view's hexcode for the latest picture matches what is being done in the background, else the user has moved on and it wont be shown.  

![Photo Gallery](/photo/Screenshot_20190509-023612_NatureFinder.jpg)
