# Contributing to MessKhata

Thanks for wanting to help! Bug reports, ideas and pull requests are all welcome.

1. Fork the repository and create a branch: `git checkout -b feature/my-change`
2. Keep the layered design: UI panels call `MessService`; only `FileDatabase` touches files.
3. If you change any calculation, update `src/test/java/.../MessServiceTest.java` and run it:
   ```bash
   javac -encoding UTF-8 -d test-out $(find src/main/java src/test/java -name "*.java")
   java -cp test-out com.messkhata.service.MessServiceTest
   ```
4. Open a pull request describing what changed and why. Screenshots help for UI changes.

Please open an [issue](https://github.com/feroz-afm/MessKhata-Mess-Meal-Manager/issues) first for bigger changes.
