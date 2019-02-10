import React from 'react';

import { makeStyles } from '@material-ui/core/styles';
import {
  Grid, Card, CardActionArea, CardActions, CardContent, CardMedia, Button, Typography,
} from '@material-ui/core';

const useStyles = makeStyles({
  card: {
    maxWidth: 345,
  },
});

function AdminMain() {
  const classes = useStyles();

  return (
    <Grid
      container
      direction="column-reverse"
      justify="space-between"
      alignItems="center"
    >
      <Card className={classes.card}>
        <CardActionArea>
          <CardMedia
            component="img"
            alt="Scalest Logo"
            height="250"
            image={`${process.env.REACT_APP_API_URL}/scalest_logo`}
            title="Scalest Logo"
          />
          <CardContent>
            <Typography gutterBottom variant="h5" component="h2">
                            Scalest
            </Typography>
            <Typography variant="body2" color="textSecondary" component="p">
                Scalest is React based CMS with strong emphasis on
                performance and handling all cases
            </Typography>
          </CardContent>
        </CardActionArea>
        <CardActions>
          <Button size="small" color="primary">
                        Share
          </Button>
          <Button size="small" color="primary">
                        Learn More
          </Button>
        </CardActions>
      </Card>
    </Grid>
  );
}

export default AdminMain;
