# An Android APK to generate possible passwords

This is a very simple app that uses a subset of words from the Linux file `/usr/share/dict/words` (from the `dictionaries-common` package) that are between 4 and 6 characters long and consist of alphabetical characters.

It uses these words to generate random, memorable combinations.

I was motivated as I was running out of imagination for this form of password content. I noticed that in one place that I had worked, the password to an important resource was actually the *What 3 Words* of the business location.

This app generates passwords that could be memorable but do not relate to your personal experience or life, making them harder to guess by someone who knows you.

The app shows a list of possible passwords; you can tap one to copy it to your clipboard for use in your password manager.

![screen shot](passgen.png "The app running")

### Key Features
- **Adjustable Word Count:** Generate passwords with 2 to 6 words.
- **Custom Delimiters:** Select your preferred separator (`-`, `.`, `_`, `/`, Space).
- **Optional Digits:** Append random numbers (00-99) for increased complexity.
- **Haptic Feedback:** Tactile confirmation when generating or copying passwords.
- **Dark Mode Support:** Full Material 3 dynamic color integration.

### Development

This is an Android Studio Project, so you should use it to edit the code. The code is written in Kotlin and uses Gradle to manage dependencies.

From the project folder, this is the command originally used to populate the word list under Linux (though you do need to have dictionaries-common installed):

    grep -E '^[a-z|A-Z]{4,6}$' /usr/share/dict/words > src/main/resources/words.txt

If you are working in WSL the dictionaries-common package is normally not installed but is available.

### Licence

GNU Public Licence 3: https://www.gnu.org/licenses/gpl-3.0.en.html

### Privacy Policy for PassGen

1. **Data Collection:** PassGen does not collect, store, or transmit any personal user data.
2. **Offline Usage:** All password generation happens locally on your device.
3. **Permissions:** The app uses no special permissions (No Internet, No Contacts, No Location).
4. **Third Parties:** No data is shared with third parties.