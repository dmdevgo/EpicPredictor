# Epic Predictor

Epic Predictor is a small REST client for Jira that allows you to forecast the completion of epics.

## Model

The forecast is based on cumulative issues flow ([NoEstimates](https://youtu.be/QVBlnCTu9Ms?feature=shared)).

Calculate the average velocity for the last few sprints and and assuming how quickly the backlog will grow, you can draw a cumulative flow diagram.

The weak point of the calculation is the choice of the backlog growth rate factor.

This tool is a predictor, but you can be a corrector. Think about how you can increase a velocity or reduce backlog to improve the forecast.

## Usage

### Configuration

Set parameters in `local.properties`:
- `jira.url` - Jira base url (scheme + host + port).
- `jira.token` - personal access token.
- `jira.epicKeys` - epic ids or keys separated by commas.

### Launch App

Run the desktop application:

```shell
./gradlew :composeApp:run
```

### Calculation Parameters

Choose calculation parameters:

- `Last Sprints Count` - The number of sprints used to calculate the average velocity.
- `Backlog Growth Rate Factor` - Used to predict the growth of the backlog. Measured in fractions of velocity.

## License

This tool is licensed under [MIT](LICENSE).

