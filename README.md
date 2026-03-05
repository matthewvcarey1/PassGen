# An Android APK to generate possible passwords

This is a very simple app that uses the subset of words from the Linux file /usr/share/dict/words (which is from the GPL3 package dictionaries-common) that are between 4 and 6 characters long and consist of alphabetical characters.

It uses these words to generate combinations of the words randomly that could be memorable.

I was motivated as I was running out imagination for this form of password content. I noticed that in one place that I had worked, that the password to an important resource was actually the What 3 Words of the business location.

This app generates passwords that could be memorable, but do not relate to your experience or life, so could not so easily be guessed by someone who knew you.

The app shows a list of possible passwords and you can pick one and copy it to your clipboard for use and adding to your password manager.

![screen shot](passgen.png "The app running")

### Development

This is an AndroidStudio Project, so you should probably use it to edit the code.

The code is in Kotlin, and it uses Gradle to manage dependencies.

From the project folder, this is the command originally used populate the word list under Linux:

    grep -E '^[a-z|A-Z]{4,6}$' /usr/share/dict/words > src/main/resources/words.txt

### Licence
  
GNU Public Licence 3 https://www.gnu.org/licenses/gpl-3.0.en.html

### Privacy Policy for PassGen

1. Data Collection: PassGen does not collect, store, or transmit any personal user data.

1. Offline Usage: All password generation happens locally on your device.

1. Permissions: The app uses no special permissions (No Internet, No Contacts, No Location).

1. Third Parties: No data is shared with third parties.