// import React, { useState, useEffect, useCallback } from 'react';
// import axios from 'axios';
// import { Box, Paper, Typography, List, ListItem, ListItemText, CircularProgress, Alert, Snackbar } from '@mui/material';

// // Helper function to debounce API calls
// const useDebounce = (callback, delay) => {
//   const [timer, setTimer] = useState(null);

//   return useCallback((...args) => {
//     if (timer) clearTimeout(timer); // Clear previous timer
//     setTimer(setTimeout(() => callback(...args), delay)); // Set new timer
//   }, [callback, delay, timer]); // Add timer to dependencies
// };

// const TweetList = ({ tweets, setTweets }) => {
//   const [loading, setLoading] = useState(false); // Manage loading state
//   const [error, setError] = useState(null);
//   const [openSnackbar, setOpenSnackbar] = useState(false); // Snackbar visibility

//   const fetchTweets = useCallback(async () => {
//     if (loading) return; // Prevent fetching if already loading

//     setLoading(true);
//     setError(null);

//     try {
//       const response = await axios.get('http://localhost:8080/api/tweets', {
//         headers: {
//           'Authorization': `Bearer ${localStorage.getItem('token')}`,
//         },
//       });

//       const newTweets = response.data || [];
//       setTweets((prevTweets) => {
//         const newTweetMap = new Map();
//         [...prevTweets, ...newTweets].forEach((tweet) => {
//           newTweetMap.set(tweet.id, tweet); // Prevent duplicates based on tweet.id
//         });
//         return Array.from(newTweetMap.values());
//       });
//     } catch (error) {
//       console.error('Error fetching tweets:', error);
//       setError(error.message);
//       setOpenSnackbar(true); // Show error in Snackbar
//     } finally {
//       setLoading(false);
//     }
//   }, [loading, setTweets]);

//   const debouncedFetchTweets = useDebounce(fetchTweets, 500); // Debounce API call

//   useEffect(() => {
//     fetchTweets(); // Initial fetch when the component mounts
//   }, [fetchTweets]);

//   const handleScroll = useCallback(() => {
//     if (
//       window.innerHeight + document.documentElement.scrollTop >=
//       document.documentElement.offsetHeight - 100 // Trigger near bottom of the page
//     ) {
//       debouncedFetchTweets(); // Use debounced version of the fetchTweets function
//     }
//   }, [debouncedFetchTweets]); // Memoize the scroll handler

//   useEffect(() => {
//     // Add scroll listener only once
//     window.addEventListener('scroll', handleScroll);

//     return () => {
//       // Clean up scroll event listener
//       window.removeEventListener('scroll', handleScroll);
//     };
//   }, [handleScroll]); // Add handleScroll to the dependency array

//   const handleCloseSnackbar = () => {
//     setOpenSnackbar(false); // Close the Snackbar
//   };

//   return (
//     <Box sx={{ margin: 2 }}>
//       <Paper sx={{ padding: 2 }}>
//         <Typography variant="h5" gutterBottom>
//           Recent Tweets
//         </Typography>
//         <List>
//           {tweets.map((tweet, index) => (
//             <ListItem key={tweet.id || index} divider>
//               <ListItemText
//                 primary={tweet.content}
//                 secondary={`By ${tweet.user?.username || 'Unknown'} at ${tweet.timestamp ? new Date(tweet.timestamp).toLocaleString() : 'Unknown time'}`}
//               />
//             </ListItem>
//           ))}
//         </List>
//         {loading && <CircularProgress sx={{ display: 'block', margin: '0 auto' }} />}
//       </Paper>

//       {error && (
//         <Snackbar
//           open={openSnackbar}
//           autoHideDuration={6000}
//           onClose={handleCloseSnackbar}
//         >
//           <Alert onClose={handleCloseSnackbar} severity="error">
//             Error loading tweets: {error}
//           </Alert>
//         </Snackbar>
//       )}
//     </Box>
//   );
// };

// export default TweetList;
import React, { useState, useEffect, useCallback, useMemo } from 'react';
import axios from 'axios';
import _ from 'lodash'; // Import lodash for throttle
import { 
  Box, 
  Paper, 
  Typography, 
  List, 
  ListItem, 
  ListItemText, 
  CircularProgress, 
  Alert, 
  Snackbar 
} from '@mui/material';

const TweetList = ({ tweets, setTweets }) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [openSnackbar, setOpenSnackbar] = useState(false);
  const [hasMore, setHasMore] = useState(true);

  const fetchTweets = useCallback(async () => {
    if (loading || !hasMore) return;

    setLoading(true);
    setError(null);

    try {
      const response = await axios.get('http://localhost:8080/api/tweets', {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
        },
      });

      const newTweets = response.data || [];
      if (newTweets.length === 0) {
        setHasMore(false);
      } else {
        setTweets((prevTweets) => {
          const newTweetMap = new Map();
          [...prevTweets, ...newTweets].forEach((tweet) => {
            newTweetMap.set(tweet.id, tweet);
          });
          return Array.from(newTweetMap.values());
        });
      }
    } catch (error) {
      console.error('Error fetching tweets:', error);
      setError(error.message);
      setOpenSnackbar(true);
    } finally {
      setLoading(false);
    }
  }, [loading, hasMore, setTweets]);

  const throttledHandleScroll = useMemo(
    () =>
      _.throttle(() => {
        if (
          window.innerHeight + document.documentElement.scrollTop >=
          document.documentElement.offsetHeight - 100
        ) {
          fetchTweets();
        }
      }, 300),
    [fetchTweets] // Include `fetchTweets` as a dependency
  );

  useEffect(() => {
    window.addEventListener('scroll', throttledHandleScroll);

    return () => {
      window.removeEventListener('scroll', throttledHandleScroll);
      throttledHandleScroll.cancel(); // Cancel throttled function to avoid memory leaks
    };
  }, [throttledHandleScroll]);

  const handleCloseSnackbar = () => {
    setOpenSnackbar(false);
  };

  return (
    <Box sx={{ margin: 2 }}>
      <Paper sx={{ padding: 2 }}>
        <Typography variant="h5" gutterBottom>
          Recent Tweets
        </Typography>
        <List>
          {tweets.map((tweet, index) => (
            <ListItem key={tweet.id || index} divider>
              <ListItemText
                primary={tweet.content}
                secondary={`By ${tweet.user?.username || 'Unknown'} at ${
                  tweet.timestamp ? new Date(tweet.timestamp).toLocaleString() : 'Unknown time'
                }`}
              />
            </ListItem>
          ))}
        </List>
        {loading && <CircularProgress sx={{ display: 'block', margin: '0 auto' }} />}
      </Paper>

      {error && (
        <Snackbar
          open={openSnackbar}
          autoHideDuration={6000}
          onClose={handleCloseSnackbar}
        >
          <Alert onClose={handleCloseSnackbar} severity="error">
            Error loading tweets: {error}
          </Alert>
        </Snackbar>
      )}
    </Box>
  );
};

export default TweetList;
