// import React, { useState } from "react";
// import axios from "axios";

// const UserSearch = () => {
//   const [username, setUsername] = useState("");
//   const [userDetails, setUserDetails] = useState(null);
//   const [tweets, setTweets] = useState([]);
//   const [loading, setLoading] = useState(false);
//   const [error, setError] = useState(null);

//   const handleSearch = async () => {
//     setLoading(true);
//     setError(null); // Reset error message
//     const token = localStorage.getItem("token");
//     try {
//       const result = await axios.get(
//         `http://localhost:8080/api/search/user/${username}`,
//         {
//           headers: {
//             Authorization: `Bearer ${token}`, // Pass token in the Authorization header
//           },
//         }
//       );

//       // console.log(result.data); // Log the response data
//       // Check if response contains user details and tweets
//       if (result.data && result.data.user) {
//         setUserDetails(result.data.user);
//         setTweets(result.data.tweets);
//       } else {
//         setError("User not found.");
//         setUserDetails(null);
//         setTweets([]);
//       }
//     } catch (err) {
//       console.error(err); // Log the error for debugging
//       setError("User not found or an error occurred.");
//       setUserDetails(null);
//       setTweets([]);
//     } finally {
//       setLoading(false);
//     }
//   };

//   return (
//     <div style={styles.container}>
//       <h1>Search for a User</h1>
//       <div style={styles.inputContainer}>
//         <input
//           type="text"
//           value={username}
//           onChange={(e) => setUsername(e.target.value)}
//           placeholder="Enter username"
//           style={styles.input}
//         />
//         <button onClick={handleSearch} style={styles.searchButton}>
//           Search
//         </button>
//       </div>

//       {loading && <p>Loading...</p>}
//       {error && <p style={styles.error}>{error}</p>}

//       {userDetails && (
//         <div style={styles.userDetails}>
//           <h2>{userDetails.username}</h2>
//           <p>Role: {userDetails.role}</p>
//         </div>
//       )}

//       {tweets.length > 0 ? (
//         <ul style={styles.tweetList}>
//           {tweets.map((tweet) => (
//             <li key={tweet.id} style={styles.tweetItem}>
//               <p>{tweet.content}</p>
//               <p style={styles.timestamp}>
//                 Posted on: {new Date(tweet.timestamp).toLocaleString()}
//               </p>
//             </li>
//           ))}
//         </ul>
//       ) : (
//         <p>No tweets found.</p>
//       )}
//     </div>
//   );
// };

// const styles = {
//   container: {
//     padding: "20px",
//     maxWidth: "600px",
//     margin: "0 auto",
//     backgroundColor: "white",
//     borderRadius: "8px",
//     boxShadow: "0 2px 10px rgba(0, 0, 0, 0.1)",
//   },
//   inputContainer: {
//     marginBottom: "20px",
//     display: "flex",
//     alignItems: "center",
//   },
//   input: {
//     padding: "10px",
//     width: "300px",
//     border: "1px solid #ccc",
//     borderRadius: "4px",
//   },
//   searchButton: {
//     marginLeft: "10px",
//     padding: "10px",
//     backgroundColor: "#007bff",
//     color: "white",
//     border: "none",
//     borderRadius: "4px",
//     cursor: "pointer",
//   },
//   userDetails: {
//     marginBottom: "20px",
//   },
//   tweetList: {
//     listStyleType: "none",
//     padding: 0,
//   },
//   tweetItem: {
//     padding: "10px",
//     border: "1px solid #ccc",
//     marginBottom: "10px",
//     borderRadius: "4px",
//   },
//   timestamp: {
//     fontSize: "12px",
//     color: "#888",
//   },
//   error: {
//     color: "red",
//   },
// };

// export default UserSearch;

import React, { useState } from "react";
import axios from "axios";

const UserSearch = () => {
  const [username, setUsername] = useState("");
  const [userDetails, setUserDetails] = useState(null);
  const [tweets, setTweets] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Declare handleSearch function before JSX
  const handleSearch = async () => {
    setLoading(true);
    setError(null); // Reset error message
    const token = localStorage.getItem("token");
    try {
      const result = await axios.get(
        `http://localhost:8080/api/search/user/${username}`,
        {
          headers: {
            Authorization: `Bearer ${token}`, // Pass token in the Authorization header
          },
        }
      );

      // Check if response contains user details and tweets
      if (result.data && result.data.user) {
        setUserDetails(result.data.user);
        setTweets(result.data.tweets);
      } else {
        setError("User not found.");
        setUserDetails(null);
        setTweets([]);
      }
    } catch (err) {
      console.error(err); // Log the error for debugging
      setError("User not found or an error occurred.");
      setUserDetails(null);
      setTweets([]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={styles.container}>
      <h1>Search for a User</h1>
      <div style={styles.inputContainer}>
        <input
          type="text"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          placeholder="Enter username"
          style={styles.input}
        />
        <button onClick={handleSearch} style={styles.searchButton}>
          Search
        </button>
      </div>

      {loading && <p>Loading...</p>}
      {error && <p style={styles.error}>{error}</p>}

      {userDetails && (
        <div style={styles.userDetails}>
          <h2>{userDetails.username}</h2>
          <p>Role: {userDetails.role}</p>
        </div>
      )}

      {tweets.length > 0 ? (
        <ul style={styles.tweetList}>
          {tweets.map((tweet) => (
            <li key={tweet.id} style={styles.tweetItem}>
              <p>{tweet.content}</p>
              <p style={styles.timestamp}>
                Posted on: {new Date(tweet.timestamp).toLocaleString()}
              </p>
            </li>
          ))}
        </ul>
      ) : (
        <p>No tweets found.</p>
      )}
    </div>
  );
};

const styles = {
  container: {
    padding: "20px",
    maxWidth: "600px",
    margin: "0 auto",
    backgroundColor: "white",
    borderRadius: "8px",
    boxShadow: "0 2px 10px rgba(0, 0, 0, 0.1)",
  },
  inputContainer: {
    marginBottom: "20px",
    display: "flex",
    alignItems: "center",
  },
  input: {
    padding: "10px",
    width: "300px",
    border: "1px solid #ccc",
    borderRadius: "4px",
  },
  searchButton: {
    marginLeft: "10px",
    padding: "10px",
    backgroundColor: "#007bff",
    color: "white",
    border: "none",
    borderRadius: "4px",
    cursor: "pointer",
  },
  userDetails: {
    marginBottom: "20px",
  },
  tweetList: {
    listStyleType: "none",
    padding: 0,
  },
  tweetItem: {
    padding: "10px",
    border: "1px solid #ccc",
    marginBottom: "10px",
    borderRadius: "4px",
  },
  timestamp: {
    fontSize: "12px",
    color: "#888",
  },
  error: {
    color: "red",
  },
};

export default UserSearch;
